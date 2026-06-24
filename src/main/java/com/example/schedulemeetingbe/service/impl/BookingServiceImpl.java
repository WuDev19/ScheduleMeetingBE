package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.*;
import com.example.schedulemeetingbe.design_pattern.command.booking.approve.BookingApproveCommandFactory;
import com.example.schedulemeetingbe.design_pattern.command.booking.rollback.BookingRollbackCommandFactory;
import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.booking.*;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.booking.*;
import com.example.schedulemeetingbe.dto.response.equipment.EquipmentAndQuantityResponse;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.composite_key.BookingAttendeeId;
import com.example.schedulemeetingbe.entity.payload.AddBookingEquipmentPayload;
import com.example.schedulemeetingbe.entity.payload.ReceiverEmailPayload;
import com.example.schedulemeetingbe.entity.payload.UpdateBookingChangePayload;
import com.example.schedulemeetingbe.entity.payload.UpdateBookingEquipmentQuantityPayload;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.exception.custom_exception.ExceedEquipmentException;
import com.example.schedulemeetingbe.exception.custom_exception.OverlapBookingException;
import com.example.schedulemeetingbe.helper.CreatePayloadHelper;
import com.example.schedulemeetingbe.mapper.BookingMapper;
import com.example.schedulemeetingbe.repository.*;
import com.example.schedulemeetingbe.service.base.*;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final OutboxEventRepository outboxEventRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final BookingAttendeeRepository bookingAttendeeRepository;

    private final IUserService iUserService;
    private final IRoomService iRoomService;
    private final IEquipmentService iEquipmentService;
    private final INotificationService iNotificationService;

    private final JsonMapper jsonMapper;
    private final BookingRollbackCommandFactory factory;
    private final BookingApproveCommandFactory approveFactory;

    private static final String BOOKING_ID = "bookingId";

    @Transactional
    @Override
    public BookingResponse createBooking(CreateBookingRequest request, String username) {
        if (request.receivers() != null && !request.attendee().equals(request.receivers().size())) {
            throw new BusinessException(ErrorResponse.INCONSISTENCY_ATTENDEE);
        }
        //kiểm tra ngày bắt đầu phải nhỏ hơn ngày kết thúc
        if (request.start().isAfter(request.end())) {
            throw new BusinessException(ErrorResponse.START_END_DATE_ERROR);
        }
        //kiểm tra người dùng có thật sự tồn tại ko
        User user = iUserService.getDetail(request.userId()).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        //kiểm tra xem có phải là chính xác người gửi request đặt phòng không (dựa vào username trong jwt và username lấy được từ userId)
        if (!username.equals(user.getUsername())) {
            throw new BusinessException(ErrorResponse.FAKE_AUTH_ERROR);
        }
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

        UpdateBookingChangePayload payload = CreatePayloadHelper.create(
                booking,
                user.getUserId(),
                room.getRoomId()
        );

        //lưu vết lịch sử đặt phòng, thay đổi phòng phục vụ cho APPROVER so sánh trực quan để dễ phê duyệt
        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(saved)
                .actionType(BookingActionType.CREATED)
                .changedBy(user)
                .newData(jsonMapper.valueToTree(payload))
                .build();
        bookingHistoryRepository.save(bookingHistory);

        if (request.receivers() != null) {
            createOutboxEvent(request.receivers(), saved, room);
        }

        return BookingMapper.mapToBookingResponse(saved, user, room);
    }

    private void createOutboxEvent(List<String> receivers, Booking booking, Room room) {
        Building building = room.getBuilding();
        ReceiverEmailPayload payload = new ReceiverEmailPayload(
                booking.getBookingId(),
                booking.getTitle(),
                booking.getDescription(),
                "Tòa nhà " + building.getBuildingName() + ", " + building.getAddress(),
                "Tầng " + room.getFloorNumber() + ", phòng " + room.getRoomName(),
                booking.getStartTime().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT_NO_TZ)),
                booking.getEndTime().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT_NO_TZ)),
                receivers
        );
        OutboxEvent event = OutboxEvent.builder()
                .status(OutboxStatus.PENDING)
                .eventType(EVENT_TYPE.SEND_EMAIL_CONFIRM_PARTICIPATE.name())
                .payload(jsonMapper.valueToTree(payload))
                .build();
        outboxEventRepository.save(event);
    }

    @Transactional
    @Override
    public Map<String, Long> updateBooking(Long bookingId, UpdateBookingRequest request, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
//        if (Duration.between(TimeUtils.ZONE_DATE_TIME, booking.getStartTime())
//                .toMinutes() < 60) {
//            throw new BusinessException(ErrorResponse.UPDATE_BOOKING_ERROR);
//        }
        UpdateBookingChangePayload oldPayload = CreatePayloadHelper.create(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        if (request.title() != null) booking.setTitle(request.title());
        if (request.description() != null) booking.setDescription(request.description());
        if (request.attendeeCount() != null) {
            if (request.attendeeCount() > booking.getRoom().getCapacity()) {
                throw new BusinessException(ErrorResponse.EXCEED_ATTENDEE);
            }
            booking.setAttendeeCount(request.attendeeCount());
        }

        /* bao trọn được trường hợp chỉ đổi room hoặc chỉ đổi start-end hoặc đổi cả hai
            (room sẽ được lọc ra những room nào thỏa mãn trước dựa vào start-end)
         */
        if (request.newRoomId() != null || (request.start() != null && request.end() != null)) {
            BookingReservation bookingReservation = bookingReservationRepository
                    .findBookingReservationsByBooking_BookingId(bookingId)
                    .orElseGet(() -> {
                        BookingReservation newReservation = new BookingReservation();
                        newReservation.setBooking(booking);
                        return newReservation;
                    });
            bookingReservation.setStatus(ReservationStatus.AWAIT_APPROVE);
            bookingReservation.setOldRoom(booking.getRoom());
            bookingReservation.setOldStartTime(booking.getStartTime());
            bookingReservation.setOldEndTime(booking.getEndTime());

            if (request.newRoomId() != null) {
                //lưu lại để có thể rollback từ cập nhật nếu approver reject
                Room room = iRoomService.getRoomDetail(request.newRoomId()).orElseThrow(() ->
                        new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
                booking.setRoom(room);
                booking.setStatus(BookingStatus.PENDING);
            }
            if (request.start() != null && request.end() != null) {
                if (request.start().isAfter(request.end())) {
                    throw new BusinessException(ErrorResponse.START_END_DATE_ERROR);
                } else {
                    checkOverlap(bookingId, request.roomId(), request.start(), request.end(), false);
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
        UpdateBookingChangePayload newPayload = CreatePayloadHelper.create(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        User user = iUserService.getDetail(userId).orElse(null);

        //revoke những cái lịch sử thay đổi cũ, chỉ để hiện cái thay đổi mới nhất để cho approver duyệt
        bookingRepository.revokeAllOldChangeHistory(bookingId, BookingActionType.UPDATED);
        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(booking)
                .actionType(BookingActionType.UPDATED)
                .changedBy(user)
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

    @Transactional
    @Override
    public StatusBookingResponse cancelBooking(Long bookingId, CancelBookingRequest request, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        UpdateBookingChangePayload oldPayload = CreatePayloadHelper.create(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.reason());
        booking.setCancelledAt(TimeUtils.ZONE_DATE_TIME);
        User register = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        UpdateBookingChangePayload newPayload = CreatePayloadHelper.create(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(booking)
                .actionType(BookingActionType.CANCELLED)
                .changedBy(register)
                .oldData(jsonMapper.valueToTree(oldPayload))
                .newData(jsonMapper.valueToTree(newPayload))
                .build();
        bookingHistoryRepository.save(bookingHistory);
        return BookingMapper.mapToStatusBookingResponse(booking);
    }

    @Transactional
    @Override
    public Map<String, Object> deleteBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        User admin = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        booking.setDeletedAt(TimeUtils.ZONE_DATE_TIME);
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

    @Transactional(readOnly = true)
    @Override
    public BookingDetailResponse getBookingDetail(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        User user = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Role role = iUserService.getRoleUser(StringCommon.ADMIN).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (!user.equals(booking.getBookedBy()) && !user.getRoles().contains(role)) {
            throw new BusinessException(ErrorResponse.BOOKING_DETAIL_ERROR);
        }
        List<BookingDetailEquipmentResponse> bookingDetailEquipments = bookingEquipmentRepository
                .getBookingEquipments(bookingId);
        return BookingMapper.mapToBookingDetailResponse(booking, user, booking.getRoom(), bookingDetailEquipments);
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

        bookingRepository.revokeAllOldChangeHistory(bookingId, BookingActionType.UPDATE_EQUIP_QUANTITY);
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
    public BookingHistoryResponse getBookingHistoryDetailToApprove(Long bookingHistoryId) {
        return bookingRepository.getDetailBookingWaitingToApprove(bookingHistoryId);
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
                .isBefore(TimeUtils.ZONE_DATE_TIME)) {
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
                .joinedAt(TimeUtils.ZONE_DATE_TIME)
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

    private void addEquipmentToRoom(CreateBookingRequest request, Booking saved) {
        List<CreateBookingEquipmentRequest> bookingEquipmentRequests = request.equipments();
        if (bookingEquipmentRequests != null && !bookingEquipmentRequests.isEmpty()) {
            // lấy thông tin cơ bản của thiết bị và số lượng còn lại để check xem còn đủ để cho mượn ko
            // tránh n+1 query và sử dụng Map để truy cập phần tử với O(1)
            Map<Long, EquipmentAndQuantityResponse> equipmentAndQuantityResponses = iEquipmentService
                    .findEquipmentAndRemainingQuantity(
                            bookingEquipmentRequests
                                    .stream()
                                    .map(CreateBookingEquipmentRequest::equipmentId)
                                    .toList()
                    )
                    .stream()
                    .collect(Collectors.toMap(EquipmentAndQuantityResponse::equipmentId, Function.identity()));
            // lấy danh sách equipment vượt quá số lượng để báo cho người dùng
            List<String> exceedQuantity = new ArrayList<>();
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
        // lấy thông tin cơ bản của thiết bị và số lượng còn lại để check xem còn đủ để cho mượn ko
        // tránh n+1 query và sử dụng Map để truy cập phần tử với O(1)
        Map<Long, EquipmentAndQuantityResponse> equipmentAndQuantityResponses = iEquipmentService
                .findEquipmentAndRemainingQuantity(
                        request
                                .stream()
                                .map(UpdateEquipmentBookingRequest::equipmentId)
                                .toList()
                )
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

    private void checkOverlap(Long bookingId, Long roomId, ZonedDateTime start, ZonedDateTime end, boolean isCreate) {
        List<String> reasons;
        if (isCreate) {
            reasons = bookingRepository.checkOverlap(
                    roomId,
                    new String[]{
                            String.format(
                                    "[%s, %s)",
                                    start.toOffsetDateTime(),
                                    end.toOffsetDateTime()
                            )}
            );
        } else {
            reasons = bookingRepository.checkOverlap(
                    bookingId,
                    roomId,
                    new String[]{
                            String.format(
                                    "[%s, %s)",
                                    start.toOffsetDateTime(),
                                    end.toOffsetDateTime()
                            )}
            );
        }
        if (!reasons.isEmpty()) {
            throw new OverlapBookingException(reasons);
        }
    }

    private void checkBookingHistoryActionType(Booking booking, RollBackRequest request, User approver) {
        factory.get(request.actionType())
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

}
