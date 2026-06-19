package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingEquipmentAction;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.booking.*;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.booking.*;
import com.example.schedulemeetingbe.dto.response.equipment.EquipmentAndQuantityResponse;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.payload.AddBookingEquipmentPayload;
import com.example.schedulemeetingbe.entity.payload.BookingEquipmentQuantityPayload;
import com.example.schedulemeetingbe.entity.payload.UpdateBookingChangePayload;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.exception.custom_exception.ExceedEquipmentException;
import com.example.schedulemeetingbe.exception.custom_exception.OverlapBookingException;
import com.example.schedulemeetingbe.mapper.BookingMapper;
import com.example.schedulemeetingbe.repository.BookingEquipmentRepository;
import com.example.schedulemeetingbe.repository.BookingHistoryRepository;
import com.example.schedulemeetingbe.repository.BookingRepository;
import com.example.schedulemeetingbe.service.base.IBookingService;
import com.example.schedulemeetingbe.service.base.IEquipmentService;
import com.example.schedulemeetingbe.service.base.IRoomService;
import com.example.schedulemeetingbe.service.base.IUserService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {

    private final BookingRepository bookingRepository;
    private final BookingEquipmentRepository bookingEquipmentRepository;
    private final BookingHistoryRepository bookingHistoryRepository;

    private final IUserService iUserService;
    private final IRoomService iRoomService;
    private final IEquipmentService iEquipmentService;

    private final JsonMapper jsonMapper;

    private static final String BOOKING_ID = "bookingId";

    @Transactional
    @Override
    public BookingResponse createBooking(CreateBookingRequest request, String username) {
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
        checkOverlap(request.roomId(), request.start(), request.end());

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

        UpdateBookingChangePayload payload = createUpdateBookingPayload(
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

        return BookingMapper.mapToBookingResponse(saved, user, room);
    }

    @Transactional
    @Override
    public Map<String, Long> updateBooking(Long bookingId, UpdateBookingRequest request, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (Duration.between(TimeUtils.ZONE_DATE_TIME, booking.getStartTime())
                .toMinutes() < 60) {
            throw new BusinessException(ErrorResponse.UPDATE_BOOKING_ERROR);
        }
        UpdateBookingChangePayload oldPayload = createUpdateBookingPayload(
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
        if (request.newRoomId() != null) {
            Room room = iRoomService.getRoomDetail(request.newRoomId()).orElseThrow(() ->
                    new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
            booking.setRoom(room);
            booking.setStatus(BookingStatus.PENDING);
        }
        if (request.start() != null && request.end() != null) {
            if (request.start().isAfter(request.end())) {
                throw new BusinessException(ErrorResponse.START_END_DATE_ERROR);
            } else {
                checkOverlap(request.roomId(), request.start(), request.end());
                booking.setStartTime(request.start());
                booking.setEndTime(request.end());
                booking.setStatus(BookingStatus.PENDING);
            }
        }
        UpdateBookingChangePayload newPayload = createUpdateBookingPayload(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        User user = iUserService.getDetail(userId).orElse(null);
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
    public StatusBookingResponse approveBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        UpdateBookingChangePayload oldPayload = createUpdateBookingPayload(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        User approver = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.ZONE_DATE_TIME);
        UpdateBookingChangePayload newPayload = createUpdateBookingPayload(
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
    public StatusBookingResponse rejectBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        UpdateBookingChangePayload oldPayload = createUpdateBookingPayload(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        User approver = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        booking.setStatus(BookingStatus.REJECTED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.ZONE_DATE_TIME);
        UpdateBookingChangePayload newPayload = createUpdateBookingPayload(
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
        return BookingMapper.mapToStatusBookingResponse(booking);
    }

    @Transactional
    @Override
    public StatusBookingResponse cancelBooking(Long bookingId, CancelBookingRequest request, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        UpdateBookingChangePayload oldPayload = createUpdateBookingPayload(
                booking,
                booking.getBookedBy().getUserId(),
                booking.getRoom().getRoomId()
        );
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.reason());
        booking.setCancelledAt(TimeUtils.ZONE_DATE_TIME);
        User register = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        UpdateBookingChangePayload newPayload = createUpdateBookingPayload(
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
        UpdateBookingChangePayload payload = createUpdateBookingPayload(
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
            Long bookingEquipmentId,
            UpdateBookingEquipQuantityRequest request
    ) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        BookingEquipment bookingEquipment = bookingEquipmentRepository
                .findById(bookingEquipmentId)
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));

        BookingEquipmentQuantityPayload oldPayload = new BookingEquipmentQuantityPayload(bookingEquipmentId, bookingEquipment.getQuantity());

        EquipmentAndQuantityResponse bookingEquipmentResponse = iEquipmentService.findEquipmentAndRemainingQuantity(bookingEquipmentId);
        if (request.quantity() > bookingEquipmentResponse.remainingQuantity()) {
            throw new ExceedEquipmentException(List.of("Vượt quá số lượng, thiết bị " +
                    bookingEquipmentResponse.equipmentName() +
                    " chỉ còn trống " +
                    bookingEquipmentResponse.remainingQuantity()));
        }
        bookingEquipment.setQuantity(request.quantity());
        booking.setStatus(BookingStatus.PENDING);

        BookingEquipmentQuantityPayload newPayload = new BookingEquipmentQuantityPayload(bookingEquipmentId, bookingEquipment.getQuantity());
        User user = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        BookingHistory bookingHistory = BookingHistory.builder()
                .booking(booking)
                .actionType(BookingActionType.UPDATED)
                .changedBy(user)
                .oldData(jsonMapper.valueToTree(oldPayload))
                .newData(jsonMapper.valueToTree(newPayload))
                .build();
        bookingHistoryRepository.save(bookingHistory);

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

    private UpdateBookingChangePayload createUpdateBookingPayload(Booking booking, Long userId, Long roomId) {
        return new UpdateBookingChangePayload(
                booking.getBookingId(),
                booking.getTitle(),
                booking.getDescription(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getAttendeeCount(),
                booking.getStatus(),
                booking.getCancellationReason(),
                roomId,
                userId,
                booking.getCreatedAt()
        );
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

    private void checkOverlap(Long roomId, ZonedDateTime start, ZonedDateTime end) {
        List<String> reasons = bookingRepository.checkOverlap(
                roomId,
                new String[]{
                        String.format(
                                "[%s, %s)",
                                start.toOffsetDateTime(),
                                end.toOffsetDateTime()
                        )}
        );
        if (!reasons.isEmpty()) {
            throw new OverlapBookingException(reasons);
        }
    }

}
