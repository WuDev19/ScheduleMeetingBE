package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.*;
import com.example.schedulemeetingbe.design_pattern.command.booking.approve.BookingApproveCommandFactory;
import com.example.schedulemeetingbe.design_pattern.command.booking.rollback.BookingRollbackCommandFactory;
import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.booking.*;
import com.example.schedulemeetingbe.dto.request.room.StartEndTimeRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.booking.*;
import com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingNotificationResponse;
import com.example.schedulemeetingbe.dto.response.booking.booking_overlap.BookingOverlapProjection;
import com.example.schedulemeetingbe.dto.response.booking.booking_overlap.BookingOverlapResponse;
import com.example.schedulemeetingbe.dto.response.booking.booking_summary.BookingSummaryProjection;
import com.example.schedulemeetingbe.dto.response.booking.booking_summary.BookingSummaryResponse;
import com.example.schedulemeetingbe.dto.response.equipment.EquipmentAndQuantityResponse;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.composite_key.BookingAttendeeId;
import com.example.schedulemeetingbe.entity.payload.*;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.exception.custom_exception.ExceedEquipmentException;
import com.example.schedulemeetingbe.exception.custom_exception.OverlapBookingException;
import com.example.schedulemeetingbe.helper.CreatePayloadHelper;
import com.example.schedulemeetingbe.mapper.BookingMapper;
import com.example.schedulemeetingbe.repository.*;
import com.example.schedulemeetingbe.repository.specification.BookingSpecification;
import com.example.schedulemeetingbe.service.base.*;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {

    private final BookingRepository bookingRepository;
    private final BookingEquipmentRepository bookingEquipmentRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final BookingReservationRepository bookingReservationRepository;
    private final BookingEquipmentReservationRepository bookingEquipmentReservationRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final BookingAttendeeRepository bookingAttendeeRepository;

    private final IUserService iUserService;
    private final IRoomService iRoomService;
    private final IEquipmentService iEquipmentService;
    private final INotificationService iNotificationService;

    private final JsonMapper jsonMapper;
    private final BookingRollbackCommandFactory rollbackFactory;
    private final BookingApproveCommandFactory approveFactory;

    private static final String BOOKING_ID = "bookingId";

    //đã có gửi notification/email cho người tham gia
    @Transactional
    @Override
    public BookingResponse createBooking(CreateBookingRequest request, Long userId) {
        if (request.start().isBefore(TimeUtils.now())) {
            throw new BusinessException(ErrorResponse.START_END_DATE_BEFORE_NOW_ERROR);
        }
        if (request.receivers() != null &&
                !request.receivers().isEmpty() &&
                !request.attendee().equals(request.receivers().size())
        ) {
            throw new BusinessException(ErrorResponse.INCONSISTENCY_ATTENDEE);
        }
        //kiểm tra ngày bắt đầu phải nhỏ hơn ngày kết thúc
        if (request.start().isAfter(request.end())) {
            throw new BusinessException(ErrorResponse.START_END_DATE_ERROR);
        }
        //kiểm tra người dùng có thật sự tồn tại ko
        User user = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));

        //kiểm tra phòng họp có thật sự tồn tại ko
        Room room = iRoomService.getRoomDetail(request.roomId()).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        //kiểm tra sức chứa của phòng hiện tại
        if (request.attendee() > room.getCapacity()) {
            throw new BusinessException(ErrorResponse.EXCEED_ATTENDEE);
        }
        //kiểm tra có bị trùng lịch trong Unavailability Room hay Bookings ko (dưới db có constraint nhưng vẫn check bên be để có thể hiện lỗi thân thiện hơn)
        checkOverlap(null, request.roomId(), request.start(), request.end(), true);

        //booking mới thì ko cần lưu vào booking_reservation
        Booking booking = Booking.builder()
                .bookedBy(user)
                .attendeeCount(request.attendee())
                .description(request.description())
                .title(request.title())
                .startTime(request.start())
                .endTime(request.end())
                .room(room)
                .build();
        Booking saved = bookingRepository.save(booking);

        // người dùng đặt lịch và có chọn thêm thiết bị khi đặt lịch
        addEquipmentToRoom(request, saved);

        CreateBookingPayload payload = CreatePayloadHelper.create(
                booking,
                user.getUserId(),
                room.getRoomId(),
                request.receivers() != null ? request.receivers() : List.of(),
                request.equipments() != null ? request.equipments() : List.of()
        );

        //lưu vết lịch sử đặt phòng, thay đổi phòng phục vụ cho APPROVER so sánh trực quan để dễ phê duyệt
        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(saved)
                .actionType(BookingActionType.CREATED)
                .changedBy(user)
                .newData(jsonMapper.valueToTree(payload))
                .build();
        bookingHistoryRepository.save(bookingHistory);

        return BookingMapper.mapToBookingResponse(saved, user, room);
    }

    @Transactional
    @Override
    public Map<String, Long> updateBooking(Long bookingId, UpdateBookingRequest request, Long userId, List<String> roles) {
        User userChange = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Room oldRoom = booking.getRoom();

        if (!booking.getBookedBy().equals(userChange) && !roles.contains(StringCommon.ADMIN)) {
            throw new BusinessException(ErrorResponse.UPDATE_BOOKING_AUTH_ERROR);
        }

        if (booking.getStatus() == BookingStatus.REJECTED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException(ErrorResponse.BOOKING_STATUS_ERROR);
        }
        //còn dưới 1 tiếng thì ko cho sửa nữa
//        if (ChronoUnit.HOURS.between(TimeUtils.now(), booking.getStartTime()) < 1) {
//            throw new BusinessException(ErrorResponse.UPDATE_BOOKING_ERROR);
//        }
        List<String> emailParticipants = bookingAttendeeRepository.getAttendeeOfBooking(bookingId)
                .stream()
                .map(User::getEmail)
                .toList();
        UpdateFocusRoomOrTimePayload oldPayload = CreatePayloadHelper.createUpdateRoomOrTime(
                booking,
                booking.getBookedBy().getUserId(),
                oldRoom.getRoomId(),
                emailParticipants
        );
        if (request.isCompleted() != null) {
            if (TimeUtils.now().isBefore(booking.getStartTime())) {
                throw new BusinessException(ErrorResponse.COMPLETED_UPDATE_BOOKING_ERROR);
            }
            booking.setStatus(BookingStatus.COMPLETED);
        }
        if (request.title() != null) booking.setTitle(request.title());
        if (request.description() != null) booking.setDescription(request.description());
        if (request.attendeeCount() != null && request.newRoomId() == null) {
            if (request.attendeeCount() > oldRoom.getCapacity()) {
                throw new BusinessException(ErrorResponse.EXCEED_ATTENDEE);
            }
            booking.setAttendeeCount(request.attendeeCount());
        }
        /* bao trọn được trường hợp chỉ đổi room hoặc chỉ đổi start-end hoặc đổi cả hai
            (room sẽ được lọc ra những room nào thỏa mãn trước dựa vào start-end)
         */
        boolean isChangeRoom = false;
        boolean isChangeTime = false;
        if (request.newRoomId() != null || (request.start() != null && request.end() != null)) {
            BookingReservation bookingReservation = bookingReservationRepository
                    .findBookingReservationsByBooking_BookingId(bookingId)
                    .orElseGet(() -> {
                        BookingReservation newReservation = new BookingReservation();
                        newReservation.setBooking(booking);
                        return newReservation;
                    });
            bookingReservation.setStatus(ReservationStatus.AWAIT_APPROVE);
            bookingReservation.setOldRoom(oldRoom);
            bookingReservation.setOldStartTime(booking.getStartTime());
            bookingReservation.setOldEndTime(booking.getEndTime());

            if (request.newRoomId() != null) {
                isChangeRoom = true;
                //lưu lại để có thể rollback từ cập nhật nếu approver reject
                Room newRoom = iRoomService.getRoomDetail(request.newRoomId()).orElseThrow(() ->
                        new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
                if (request.attendeeCount() != null && request.attendeeCount() > newRoom.getCapacity()) {
                    throw new BusinessException(ErrorResponse.EXCEED_ATTENDEE);
                }
                booking.setRoom(newRoom);
                booking.setStatus(BookingStatus.PENDING);
            }
            if (request.start() != null && request.end() != null) {
                if (request.start().isBefore(TimeUtils.now())) {
                    throw new BusinessException(ErrorResponse.START_END_DATE_BEFORE_NOW_ERROR);
                } else if (request.start().isAfter(request.end())) {
                    throw new BusinessException(ErrorResponse.START_END_DATE_ERROR);
                } else {
                    checkOverlap(bookingId, request.roomId(), request.start(), request.end(), false);
                    isChangeTime = true;
                    booking.setStartTime(request.start());
                    booking.setEndTime(request.end());
                    booking.setStatus(BookingStatus.PENDING);
                }
            }
            /*
             * tạo bảng booking_reservation để bảo toàn những lịch mình đặt trước khi bị thay đổi và
             * lịch mới chưa được APPROVER duyệt, khi đó người dùng khác cũng ko thể đặt được
             * lịch trùng với cả lịch mới cập nhật và lịch cũ đang đợi lịch mới đc duyệt
             * nếu ko có logic này thì
             * Ví dụ như người dùng A đổi sang room A1
             * người dùng B đăng kí vào room A1 có khoảng time overlap với giá trị cũ của A
             * thì khi yêu cầu thay đổi của A ko đc APPROVER chấp thuận sẽ được rollback về giá trị cũ
             * nhưng khi đó B đã đăng kí thành công vào lịch đó => lỗi ko mong muốn nên phải bảo toàn cả hai lịch cho A
             */
            bookingReservationRepository.save(bookingReservation);
        }

        // nếu thay đổi room hoặc time thì thêm cái emails để có thể gửi thông báo cho những người tham gia
        UpdateFocusRoomOrTimePayload newPayload = CreatePayloadHelper.createUpdateRoomOrTime(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId(), // lấy room mới vừa set()
                emailParticipants //empty là thay đổi bình thường
        );
        if (!isChangeRoom && !isChangeTime) {
            oldPayload.setEmails(List.of());
            newPayload.setEmails(List.of());
        }

        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(booking)
                .actionType(BookingActionType.UPDATED)
                .changedBy(userChange)
                .oldData(jsonMapper.valueToTree(oldPayload))
                .newData(jsonMapper.valueToTree(newPayload))
                .build();
        bookingHistoryRepository.save(bookingHistory);
        return Map.of(BOOKING_ID, bookingId);
    }

    @Transactional
    @Override
    public Map<String, Long> addEquipmentBooking(Long bookingId, List<UpdateEquipmentBookingRequest> request, Long userId) {
        List<BookingDetailEquipmentResponse> oldBookingEquipment = bookingEquipmentRepository
                .getBookingEquipments(bookingId);
        AddBookingEquipmentPayload oldPayload = new AddBookingEquipmentPayload(bookingId, oldBookingEquipment);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        List<UpdateEquipmentBookingRequest> addBookingEquipment = new ArrayList<>();
        List<UpdateEquipmentBookingRequest> deleteBookingEquipment = new ArrayList<>();
        request.forEach(updateEquipmentBookingRequest -> {
            if (updateEquipmentBookingRequest.action().equals(BookingEquipmentAction.ADD))
                addBookingEquipment.add(updateEquipmentBookingRequest);
            else
                deleteBookingEquipment.add(updateEquipmentBookingRequest);
        });
        if (!deleteBookingEquipment.isEmpty()) {
            bookingEquipmentRepository
                    .deleteBookingEquipmentsByEquipment_EquipmentIdInAndBooking_BookingId(
                            deleteBookingEquipment
                                    .stream()
                                    .map(UpdateEquipmentBookingRequest::equipmentId)
                                    .toList(),
                            bookingId
                    );
        }
        if (!addBookingEquipment.isEmpty()) {
            addEquipmentToRoom(addBookingEquipment, booking);
        }
        booking.setStatus(BookingStatus.PENDING);

        List<BookingDetailEquipmentResponse> newBookingEquipment = bookingEquipmentRepository.getBookingEquipments(bookingId);
        AddBookingEquipmentPayload newPayload = new AddBookingEquipmentPayload(bookingId, newBookingEquipment);
        User user = iUserService.getDetail(userId).orElse(null);

        // add equipment thì ko cần revoke lịch sử
        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(booking)
                .actionType(BookingActionType.ADD_EQUIPMENT)
                .changedBy(user)
                .oldData(jsonMapper.valueToTree(oldPayload))
                .newData(jsonMapper.valueToTree(newPayload))
                .build();
        bookingHistoryRepository.save(bookingHistory);

        return Map.of(BOOKING_ID, bookingId);
    }

    @Transactional
    @Override
    public StatusBookingResponse approveBooking(Long bookingId, ApproveRequest request, Long userId) {
        long start = System.currentTimeMillis();
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        UpdateBookingChangePayload oldPayload = CreatePayloadHelper.create(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        User approver = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        approveFactory.get(request.actionType()).execute(booking, request, approver);
        UpdateBookingChangePayload newPayload = CreatePayloadHelper.create(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(booking)
                .actionType(BookingActionType.APPROVED)
                .changedBy(approver)
                .oldData(jsonMapper.valueToTree(oldPayload))
                .newData(jsonMapper.valueToTree(newPayload))
                .build();
        bookingHistoryRepository.save(bookingHistory);
        System.out.println("Tốc độ hàm approveBooking: " + (System.currentTimeMillis() - start) + "ms");
        return BookingMapper.mapToStatusBookingResponse(booking);
    }

    @Transactional
    @Override
    public StatusBookingResponse rejectBooking(Long bookingId, RollBackRequest request, Long userId) {
        long start = System.currentTimeMillis();
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        UpdateBookingChangePayload oldPayload = CreatePayloadHelper.create(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        User approver = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        checkBookingHistoryActionType(booking, request, approver);
        UpdateBookingChangePayload newPayload = CreatePayloadHelper.create(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(booking)
                .actionType(BookingActionType.REJECTED)
                .changedBy(approver)
                .oldData(jsonMapper.valueToTree(oldPayload))
                .newData(jsonMapper.valueToTree(newPayload))
                .build();
        bookingHistoryRepository.save(bookingHistory);
        System.out.println((System.currentTimeMillis() - start) + " ms - Tốc độ");
        return BookingMapper.mapToStatusBookingResponse(booking);
    }

    //đã có gửi notification/email cho người tham gia
    @Transactional
    @Override
    public StatusBookingResponse cancelBooking(Long bookingId, CancelBookingRequest request, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (ChronoUnit.MINUTES.between(TimeUtils.now(), booking.getStartTime()) <= 30) {
            throw new BusinessException(ErrorResponse.UPDATE_BOOKING_ERROR);
        }
        if (booking.getStatus() != BookingStatus.PENDING &&
                booking.getStatus() != BookingStatus.APPROVED
        ) {
            throw new BusinessException(ErrorResponse.BOOKING_CANCEL_ERROR);
        }
        List<User> users = bookingAttendeeRepository.getAttendeeOfBooking(bookingId);
        List<String> emails = bookingAttendeeRepository.getEmailAttendee(bookingId);
        Room room = booking.getRoom();
        UpdateBookingChangePayload oldPayload = CreatePayloadHelper.create(
                booking,
                booking.getBookedBy().getUserId(),
                room.getRoomId()
        );
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.reason());
        booking.setCancelledAt(TimeUtils.now());
        User register = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        UpdateBookingChangePayload newPayload = CreatePayloadHelper.create(
                booking,
                booking.getBookedBy().getUserId(),
                room.getRoomId()
        );
        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(booking)
                .actionType(BookingActionType.CANCELLED)
                .changedBy(register)
                .oldData(jsonMapper.valueToTree(oldPayload))
                .newData(jsonMapper.valueToTree(newPayload))
                .build();
        bookingHistoryRepository.save(bookingHistory);
        createEventCancel(booking, room, request.reason(), users, emails);
        return BookingMapper.mapToStatusBookingResponse(booking);
    }

    private void createEventCancel(Booking booking, Room room, String reason, List<User> users, List<String> emails) {
        Building building = room.getBuilding();
        List<Notification> notifications = new ArrayList<>();
        users.forEach(user -> {
            Notification notification = Notification.builder()
                    .title(StringCommon.TITLE_NOTIFICATION)
                    .booking(booking)
                    .message(reason)
                    .user(user)
                    .build();
            notifications.add(notification);
        });
        iNotificationService.save(notifications);
        CancelBookingPayload payload = new CancelBookingPayload(
                booking.getBookingId(),
                booking.getTitle(),
                "Tòa nhà " + building.getBuildingName() + ", " + building.getAddress(),
                "Tầng " + room.getFloorNumber() + ", phòng " + room.getRoomName(),
                booking.getStartTime().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT_NO_TZ)),
                booking.getEndTime().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT_NO_TZ)),
                emails,
                reason
        );
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(EventType.CANCEL_BOOKING_BY_REGISTER.name())
                .status(OutboxStatus.PENDING)
                .payload(jsonMapper.valueToTree(payload))
                .build();
        outboxEventRepository.save(outboxEvent);
    }

    @Transactional
    @Override
    public Map<String, Object> deleteBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        User admin = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        booking.setDeletedAt(TimeUtils.now());
        UpdateBookingChangePayload payload = CreatePayloadHelper.create(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(booking)
                .actionType(BookingActionType.DELETE)
                .changedBy(admin)
                .oldData(jsonMapper.valueToTree(payload))
                .build();
        bookingHistoryRepository.save(bookingHistory);
        return CRUDResponseHelper.deleteSuccess();
    }

    @Transactional
    @Override
    public Map<String, Object> addParticipants(Long bookingId, List<String> emails) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (booking.getStatus() != BookingStatus.APPROVED &&
                booking.getStatus() != BookingStatus.PENDING
        ) {
            throw new BusinessException(ErrorResponse.BOOKING_STATUS_ERROR);
        }
        Room room = booking.getRoom();
        Building building = room.getBuilding();
        OutboxEvent event = OutboxEvent.builder()
                .status(OutboxStatus.PENDING)
                .eventType(EventType.SEND_EMAIL_CONFIRM_PARTICIPATE.name())
                .payload(jsonMapper.valueToTree(
                        CreatePayloadHelper.createReceiverEmailPayload(
                                booking,
                                building,
                                room,
                                emails
                        ))
                )
                .build();
        outboxEventRepository.save(event);
        return CRUDResponseHelper.createSuccess();
    }

    // ko cho xem lịch của người khác, chỉ hiện preview vài thông tin (nội bộ nên có thể triển khai như này)
    @Transactional(readOnly = true)
    @Override
    public BookingDetailResponse getBookingDetail(Long bookingId, Long userId) {
        //phải call user trước, tại trong cùng 1 transaction bên booking cũng có user có id là 4
        //mà user bên booking để lazy nên nó tạo proxy có data là null
        User user = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        List<User> users = bookingAttendeeRepository.getAttendeeOfBooking(bookingId);
        Set<String> myRole = iUserService.getMyRole(userId);
        boolean isPrivileged = myRole.contains(StringCommon.ADMIN) ||
                myRole.contains(StringCommon.APPROVER) ||
                users.contains(user);
        if (!user.equals(booking.getBookedBy()) && !isPrivileged) {
            throw new BusinessException(ErrorResponse.BOOKING_DETAIL_ERROR);
        }
        List<BookingDetailEquipmentResponse> bookingDetailEquipments = bookingEquipmentRepository
                .getBookingEquipments(bookingId);
        return BookingMapper.mapToBookingDetailResponse(booking, booking.getBookedBy(), booking.getRoom(), bookingDetailEquipments);
    }

    @Transactional
    @Override
    public BookingEquipmentResponse updateBookingEquipmentQuantity(
            Long bookingId,
            Long userId,
            Long equipmentId,
            Long bookingEquipmentId,
            UpdateBookingEquipQuantityRequest request
    ) {
        long start = System.currentTimeMillis();
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        BookingEquipment bookingEquipment = bookingEquipmentRepository
                .findById(bookingEquipmentId)
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));

        UpdateBookingEquipmentQuantityPayload oldPayload = new UpdateBookingEquipmentQuantityPayload(bookingEquipmentId, bookingEquipment.getQuantity());

        //kiểm tra khi tăng số lượng với giảm số lượng thì có thỏa mãn ko
        validateEquipmentQuantity(bookingEquipment, equipmentId, request.quantity());

        booking.setStatus(BookingStatus.PENDING);

        UpdateBookingEquipmentQuantityPayload newPayload = new UpdateBookingEquipmentQuantityPayload(bookingEquipmentId, bookingEquipment.getQuantity());
        User user = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));

        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(booking)
                .actionType(BookingActionType.UPDATE_EQUIP_QUANTITY)
                .changedBy(user)
                .oldData(jsonMapper.valueToTree(oldPayload))
                .newData(jsonMapper.valueToTree(newPayload))
                .build();
        bookingHistoryRepository.save(bookingHistory);

        System.out.println((System.currentTimeMillis() - start) + " ms - Tốc độ");
        return new BookingEquipmentResponse(bookingEquipmentId, request.quantity());
    }

    @Override
    public PageResponse<BookingSummaryResponse> getBookingWaitingApprove(Pageable pageable) {
        Page<BookingSummaryProjection> page = bookingRepository.getBookingWaitingApprove(pageable);
        List<BookingSummaryResponse> result = page.getContent()
                .stream()
                .map(BookingMapper::mapToBookingSummaryResponse)
                .toList();
        return new PageResponse<>(
                page.getNumber(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                result
        );
    }

    @Override
    public BookingHistoryResponse getBookingHistoryDetailToApprove(Long historyId) {
        return bookingRepository.getDetailBookingWaitingToApprove(historyId);
    }

    @Transactional
    @Override
    public void verifyEmailAndUpsertBookingAttendee(String token, Long bookingId) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorResponse.EMAIL_LINK_UNAVAILABILITY));
        if (verificationToken.getVerified()) return;
        if (verificationToken.getRevoked()) {
            throw new BusinessException(ErrorResponse.VERIFY_TOKEN_REVOKED);
        }
        if (verificationToken.getExpiresAt()
                .isBefore(TimeUtils.now())) {
            verificationToken.setRevoked(true);
            throw new BusinessException(ErrorResponse.VERIFY_TOKEN_EXPIRED);
        }
        User user = verificationToken.getUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        BookingAttendee bookingAttendee = BookingAttendee.builder()
                .id(new BookingAttendeeId(bookingId, user.getUserId()))
                .booking(booking)
                .user(user)
                .joinedAt(TimeUtils.now())
                .build();
        verificationToken.setVerified(true);
        verificationToken.setRevoked(true);
        bookingAttendeeRepository.save(bookingAttendee);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingNotificationResponse getBookingAndNotification(Long bookingId, Long userId, Long notificationId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        User user = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Notification notification = iNotificationService.getNotification(notificationId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        return BookingMapper.mapToBookingNotificationResponse(booking, booking.getRoom(), user, notification);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<BookingResponse> filterBooking(BookingFilterRequest request, Pageable pageable) {
        long start = System.currentTimeMillis();
        OffsetDateTime fromDate = parseOffsetDateTime(request.fromDate());
        OffsetDateTime toDate = parseOffsetDateTime(request.toDate());
        Page<Booking> page = bookingRepository.findAll(
                BookingSpecification.filter(
                        request.roomId(),
                        request.bookedBy(),
                        request.status(),
                        fromDate,
                        toDate
                ),
                pageable
        );
        List<BookingResponse> content = page.getContent()
                .stream()
                .map(booking -> BookingMapper.mapToBookingResponse(
                        booking,
                        booking.getBookedBy(),
                        booking.getRoom()
                ))
                .toList();
        System.out.println("Tốc độ filter: " + (System.currentTimeMillis() - start));
        return new PageResponse<>(
                page.getNumber(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                content
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponse> viewBookings(BookingViewRequest request) {
        LocalDate targetDate = request.targetDate();
        if (request.viewType() == null || targetDate == null) {
            throw new BusinessException(ErrorResponse.FIELD_INVALID);
        }
        OffsetDateTime startDateTime;
        OffsetDateTime endDateTime;
        switch (request.viewType()) {
            case DAY -> {
                startDateTime = targetDate.atStartOfDay().atOffset(TimeUtils.ZONE_OFFSET);
                endDateTime = targetDate.atTime(LocalTime.MAX).atOffset(TimeUtils.ZONE_OFFSET);
            }
            case WEEK -> {
                LocalDate monday = targetDate.with(DayOfWeek.MONDAY);
                LocalDate sunday = monday.plusDays(6);
                startDateTime = monday.atStartOfDay().atOffset(TimeUtils.ZONE_OFFSET);
                endDateTime = sunday.atTime(LocalTime.MAX).atOffset(TimeUtils.ZONE_OFFSET);
            }
            case MONTH -> {
                LocalDate firstDay = targetDate.with(TemporalAdjusters.firstDayOfMonth());
                LocalDate lastDay = targetDate.with(TemporalAdjusters.lastDayOfMonth());
                startDateTime = firstDay.atStartOfDay().atOffset(TimeUtils.ZONE_OFFSET);
                endDateTime = lastDay.atTime(LocalTime.MAX).atOffset(TimeUtils.ZONE_OFFSET);
            }
            default -> throw new BusinessException(ErrorResponse.FIELD_INVALID);
        }
        List<Booking> result = bookingRepository.findAll(
                BookingSpecification.filter(
                        null,
                        request.fullName(),
                        null,
                        startDateTime,
                        endDateTime
                )
        );
        return result.stream()
                .map(booking -> BookingMapper.mapToBookingResponse(
                        booking,
                        booking.getBookedBy(),
                        booking.getRoom()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public byte[] exportBookings(BookingExportRequest request, Long userId) {
        List<Booking> bookings;
        BookingExportType effectiveType = request.exportType() != null ? request.exportType() : BookingExportType.REGISTER;
        if (effectiveType == BookingExportType.REGISTER) {
            bookings = bookingRepository.findAllBookingsForRegisterExport(
                    userId,
                    List.of(BookingStatus.PENDING, BookingStatus.APPROVED)
            );
        } else {
            bookings = bookingRepository.findAllBookingsForApproverExport();
        }
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bookings");
            Row header = sheet.createRow(0);
            String[] headings = {
                    "Booking ID",
                    "Title",
                    "Description",
                    "Room Name",
                    "Building Address",
                    "Floor Number",
                    "Booked By",
                    "Booked Email",
                    "Booked Phone",
                    "Status",
                    "Start Time",
                    "End Time",
                    "Attendee Count"
            };
            for (int i = 0; i < headings.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headings[i]);
            }
            int rowIndex = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT);
            for (Booking booking : bookings) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(booking.getBookingId());
                row.createCell(1).setCellValue(booking.getTitle() != null ? booking.getTitle() : "");
                row.createCell(2).setCellValue(booking.getDescription() != null ? booking.getDescription() : "");
                row.createCell(3).setCellValue(booking.getRoom().getRoomName());
                row.createCell(4).setCellValue(booking.getRoom().getBuilding().getAddress());
                row.createCell(5).setCellValue(booking.getRoom().getFloorNumber());
                row.createCell(6).setCellValue(booking.getBookedBy().getFullName());
                row.createCell(7).setCellValue(booking.getBookedBy().getEmail());
                row.createCell(8).setCellValue(booking.getBookedBy().getPhone() != null ? booking.getBookedBy().getPhone() : "");
                row.createCell(9).setCellValue(booking.getStatus().name());
                row.createCell(10).setCellValue(booking.getStartTime().format(formatter));
                row.createCell(11).setCellValue(booking.getEndTime().format(formatter));
                row.createCell(12).setCellValue(booking.getAttendeeCount());
            }
            for (int i = 0; i < headings.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(ErrorResponse.FILE_ACCESS_ERROR);
        }
    }

    private OffsetDateTime parseOffsetDateTime(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException ex) {
            try {
                return OffsetDateTime.parse(dateTime, DateTimeFormatter.ofPattern(StringCommon.OFFSET_FORMAT));
            } catch (DateTimeParseException e) {
                try {
                    return OffsetDateTime.parse(dateTime + TimeUtils.ZONE_OFFSET, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } catch (DateTimeParseException ignore) {
                    throw new BusinessException(ErrorResponse.PARSE_JSON);
                }
            }
        }
    }

    @Transactional
    @Override
    public void confirmParticipateIn(Long bookingId, Long userId) {
        boolean exist = bookingAttendeeRepository.existsById(new BookingAttendeeId(bookingId, userId));
        if (exist) return;
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        User participant = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        BookingAttendee bookingAttendee = BookingAttendee.builder()
                .id(new BookingAttendeeId(bookingId, userId))
                .user(participant)
                .booking(booking)
                .joinedAt(TimeUtils.now())
                .build();
        bookingAttendeeRepository.save(bookingAttendee);
    }

    @Override
    public List<BookingOverlapResponse> getBookingOverlapRoomUnavailability(Long roomId, StartEndTimeRequest request) {
        List<BookingOverlapProjection> result = bookingRepository.getBookingOverlapRoomUnavailability(
                roomId,
                request.start(),
                request.end()
        );
        return result.stream()
                .map(BookingMapper::mapToBookingOverlapResponse)
                .toList();
    }

    @Override
    public Optional<Booking> getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    @Override
    public List<Booking> getApprovedBooking() {
        return bookingRepository.findByStatusApproved();
    }

    @Override
    public Optional<BookingRemindingResponse> getBookingReminding(Long bookingId) {
        return bookingRepository.getBookingReminding(bookingId);
    }

    private void addEquipmentToRoom(CreateBookingRequest request, Booking saved) {
        List<CreateBookingEquipmentRequest> bookingEquipmentRequests = request.equipments();
        if (bookingEquipmentRequests != null && !bookingEquipmentRequests.isEmpty()) {

            List<Long> eqIds = bookingEquipmentRequests
                    .stream()
                    .map(CreateBookingEquipmentRequest::equipmentId)
                    .toList();

            // đặt lock ở đây
            iEquipmentService.lockEquipment(eqIds);

            // lấy thông tin cơ bản của thiết bị và số lượng còn lại để check xem còn đủ để cho mượn ko
            // tránh n+1 query và sử dụng Map để truy cập phần tử với O(1)
            // xem xét đặt lock ở đây
            Map<Long, EquipmentAndQuantityResponse> equipmentAndQuantityResponses = iEquipmentService
                    .findEquipmentAndRemainingQuantity(eqIds)
                    .stream()
                    .collect(Collectors.toMap(EquipmentAndQuantityResponse::equipmentId, Function.identity()));
            // lấy danh sách equipment vượt quá số lượng để báo cho người dùng
            List<String> exceedQuantity = new ArrayList<>();
            //chỗ này n+1 query nhưng thường thì số lượng equipment gửi lên cũng ko quá nhiều nên tạm thơì vẫn để như này
            bookingEquipmentRequests.forEach(createBookingEquipmentRequest -> {
                EquipmentAndQuantityResponse equipmentAndQuantity = equipmentAndQuantityResponses.get(createBookingEquipmentRequest.equipmentId());
                //tránh trường hợp gửi equipmentId ko hợp lệ
                iEquipmentService.getEquipmentDetail(createBookingEquipmentRequest.equipmentId()).orElseThrow(() ->
                        new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
                //tránh NPE vì khi truy vấn bên trên những equipmentId ko có trong bảng bookingequipment sẽ ko xuất hiện trong result
                if (equipmentAndQuantity != null) {
                    if (createBookingEquipmentRequest.quantity() > equipmentAndQuantity.remainingQuantity()) {
                        exceedQuantity.add("Vượt quá số lượng, thiết bị " +
                                equipmentAndQuantity.equipmentName() +
                                " chỉ còn trống " +
                                equipmentAndQuantity.remainingQuantity()
                        );
                    }
                }
            });
            if (!exceedQuantity.isEmpty()) {
                throw new ExceedEquipmentException(exceedQuantity);
            }
            Map<Long, Equipment> equipments = iEquipmentService.findEquipmentIn(bookingEquipmentRequests
                            .stream()
                            .map(CreateBookingEquipmentRequest::equipmentId)
                            .toList()
                    )
                    .stream()
                    .collect(Collectors.toMap(Equipment::getEquipmentId, Function.identity()));
            List<BookingEquipment> bookingEquipments = bookingEquipmentRequests
                    .stream()
                    .map(createBookingEquipmentRequest -> BookingEquipment
                            .builder()
                            .booking(saved)
                            .quantity(createBookingEquipmentRequest.quantity())
                            .equipment(equipments.get(createBookingEquipmentRequest.equipmentId()))
                            .build())
                    .toList();
            bookingEquipmentRepository.saveAll(bookingEquipments);
        }
    }

    private void addEquipmentToRoom(List<UpdateEquipmentBookingRequest> request, Booking saved) {
        List<Long> eqIds = request
                .stream()
                .map(UpdateEquipmentBookingRequest::equipmentId)
                .toList();

        // đặt lock ở đây
        iEquipmentService.lockEquipment(eqIds);

        // lấy thông tin cơ bản của thiết bị và số lượng còn lại để check xem còn đủ để cho mượn ko
        // tránh n+1 query và sử dụng Map để truy cập phần tử với O(1)
        Map<Long, EquipmentAndQuantityResponse> equipmentAndQuantityResponses = iEquipmentService
                .findEquipmentAndRemainingQuantity(eqIds)
                .stream()
                .collect(Collectors.toMap(EquipmentAndQuantityResponse::equipmentId, Function.identity()));
        // lấy danh sách equipment vượt quá số lượng để báo cho người dùng
        List<String> exceedQuantity = new ArrayList<>();
        request.forEach(createBookingEquipmentRequest -> {
            EquipmentAndQuantityResponse equipmentAndQuantity = equipmentAndQuantityResponses.get(createBookingEquipmentRequest.equipmentId());
            //tránh trường hợp gửi equipmentId ko hợp lệ
            iEquipmentService.getEquipmentDetail(createBookingEquipmentRequest.equipmentId()).orElseThrow(() ->
                    new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
            //tránh NPE vì khi truy vấn bên trên những equipmentId ko có trong bảng bookingequipment sẽ ko xuất hiện trong result
            if (equipmentAndQuantity != null) {
                if (createBookingEquipmentRequest.quantity() > equipmentAndQuantity.remainingQuantity()) {
                    exceedQuantity.add("Vượt quá số lượng, thiết bị " +
                            equipmentAndQuantity.equipmentName() +
                            " chỉ còn trống " +
                            equipmentAndQuantity.remainingQuantity()
                    );
                }
            }
        });
        if (!exceedQuantity.isEmpty()) {
            throw new ExceedEquipmentException(exceedQuantity);
        }
        Map<Long, Equipment> equipments = iEquipmentService.findEquipmentIn(request
                        .stream()
                        .map(UpdateEquipmentBookingRequest::equipmentId)
                        .toList()
                )
                .stream()
                .collect(Collectors.toMap(Equipment::getEquipmentId, Function.identity()));
        List<BookingEquipment> bookingEquipments = request
                .stream()
                .map(createBookingEquipmentRequest -> BookingEquipment
                        .builder()
                        .booking(saved)
                        .quantity(createBookingEquipmentRequest.quantity())
                        .equipment(equipments.get(createBookingEquipmentRequest.equipmentId()))
                        .build())
                .toList();
        bookingEquipmentRepository.saveAll(bookingEquipments);
    }

    private void checkOverlap(Long bookingId, Long roomId, OffsetDateTime start, OffsetDateTime end, boolean isCreate) {
        List<String> reasons;
        if (isCreate) {
            reasons = bookingRepository.checkOverlap(
                    roomId,
                    new String[]{
                            String.format(
                                    "[%s, %s)",
                                    start,
                                    end
                            )}
            );
        } else {
            reasons = bookingRepository.checkOverlap(
                    bookingId,
                    roomId,
                    new String[]{
                            String.format(
                                    "[%s, %s)",
                                    start,
                                    end
                            )}
            );
        }
        if (!reasons.isEmpty()) {
            throw new OverlapBookingException(reasons);
        }
    }

    private void checkBookingHistoryActionType(Booking booking, RollBackRequest request, User approver) {
        rollbackFactory.get(request.actionType())
                .execute(booking, request, approver);
    }

    private void validateEquipmentQuantity(
            BookingEquipment bookingEquipment,
            Long equipmentId,
            Integer newQuantity
    ) {
        Integer currentQuantity = bookingEquipment.getQuantity();
        if (Objects.equals(currentQuantity, newQuantity)) {
            return;
        }
        iEquipmentService.getEquipmentWithLock(equipmentId);
        // tăng số lượng
        if (newQuantity > currentQuantity) {
            EquipmentAndQuantityResponse bookingEquipmentResponse =
                    iEquipmentService.findEquipmentAndRemainingQuantity(equipmentId);
            int needMore = newQuantity - currentQuantity;
            if (needMore > bookingEquipmentResponse.remainingQuantity()) {
                throw new ExceedEquipmentException(List.of("Vượt quá số lượng, thiết bị " +
                        bookingEquipmentResponse.equipmentName() +
                        " chỉ còn trống " +
                        bookingEquipmentResponse.remainingQuantity()));
            }
            bookingEquipment.setQuantity(newQuantity);
            return;
        }
        // giảm số lượng
        int reservedQuantity = currentQuantity - newQuantity;
        bookingEquipment.setQuantity(newQuantity);
        BookingEquipmentReservation reservation =
                bookingEquipmentReservationRepository
                        .findByBookingEquipment_BookingEquipmentId(
                                bookingEquipment.getBookingEquipmentId()
                        )
                        .orElse(
                                BookingEquipmentReservation.builder()
                                        .bookingEquipment(bookingEquipment)
                                        .build()
                        );
        reservation.setStatus(ReservationStatus.AWAIT_APPROVE);
        reservation.setReservationQuantity(reservedQuantity);
        bookingEquipmentReservationRepository.save(reservation);
    }

    @Override
    public List<Booking> getBookingInBookingIds(List<Long> bookingIds) {
        return bookingRepository.findBookingByBookingIdIn(bookingIds);
    }
}
