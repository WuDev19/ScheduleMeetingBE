package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.enums.BookingEquipmentAction;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.request.booking.*;
import com.example.schedulemeetingbe.dto.response.booking.BookingResponse;
import com.example.schedulemeetingbe.dto.response.booking.StatusBookingResponse;
import com.example.schedulemeetingbe.dto.response.equipment.EquipmentAndQuantityResponse;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.exception.custom_exception.ExceedEquipmentException;
import com.example.schedulemeetingbe.exception.custom_exception.OverlapBookingException;
import com.example.schedulemeetingbe.mapper.BookingMapper;
import com.example.schedulemeetingbe.repository.BookingEquipmentRepository;
import com.example.schedulemeetingbe.repository.BookingRepository;
import com.example.schedulemeetingbe.service.base.IBookingService;
import com.example.schedulemeetingbe.service.base.IEquipmentService;
import com.example.schedulemeetingbe.service.base.IRoomService;
import com.example.schedulemeetingbe.service.base.IUserService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final IUserService iUserService;
    private final IRoomService iRoomService;
    private final IEquipmentService iEquipmentService;

    private static final String BOOKING_ID = "bookingId";

    @Transactional
    @Override
    public BookingResponse createBooking(CreateBookingRequest request, String username) {
        long start = System.currentTimeMillis();
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
        System.out.println((System.currentTimeMillis() - start) + "ms Tốc độ");
        return BookingMapper.mapToBookingResponse(saved, user, room);
    }

    @Transactional
    @Override
    public Map<String, Long> updateBooking(Long bookingId, UpdateBookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (Duration.between(TimeUtils.ZONE_DATE_TIME, booking.getStartTime())
                .toMinutes() < 60) {
            throw new BusinessException(ErrorResponse.UPDATE_BOOKING_ERROR);
        }
        if (request.title() != null) booking.setTitle(request.title());
        if (request.description() != null) booking.setDescription(request.description());
        if (request.attendeeCount() != null) booking.setAttendeeCount(request.attendeeCount());
        if (request.newRoomId() != null) {
            Room room = iRoomService.getRoomDetail(request.newRoomId()).orElseThrow(() ->
                    new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
            booking.setRoom(room);
        }
        if (request.start() != null && request.end() != null) {
            if (request.start().isAfter(request.end())) {
                throw new BusinessException(ErrorResponse.START_END_DATE_ERROR);
            } else {
                checkOverlap(request.roomId(), request.start(), request.end());
                booking.setStartTime(request.start());
                booking.setEndTime(request.end());
            }
        }
        booking.setStatus(BookingStatus.PENDING);
        return Map.of(BOOKING_ID, bookingId);
    }

    @Transactional
    @Override
    public Map<String, Long> addEquipmentBooking(Long bookingId, List<UpdateEquipmentBookingRequest> request) {
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
            bookingEquipmentRepository.deleteAllByIdInBatch(deleteBookingEquipment.stream().map(UpdateEquipmentBookingRequest::equipmentId).toList());
        }
        if (!addBookingEquipment.isEmpty()) {
            addEquipmentToRoom(addBookingEquipment, booking);
        }
        booking.setStatus(BookingStatus.PENDING);
        return Map.of(BOOKING_ID, bookingId);
    }

    @Transactional
    @Override
    public StatusBookingResponse approveBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        User user = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(user);
        booking.setApprovedAt(TimeUtils.ZONE_DATE_TIME);
        return BookingMapper.mapToStatusBookingResponse(booking);
    }

    @Transactional
    @Override
    public StatusBookingResponse rejectBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        User user = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        booking.setStatus(BookingStatus.REJECTED);
        booking.setApprovedBy(user);
        booking.setApprovedAt(TimeUtils.ZONE_DATE_TIME);
        return BookingMapper.mapToStatusBookingResponse(booking);
    }

    @Transactional
    @Override
    public StatusBookingResponse cancelBooking(Long bookingId, CancelBookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.reason());
        booking.setCancelledAt(TimeUtils.ZONE_DATE_TIME);
        return BookingMapper.mapToStatusBookingResponse(booking);
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
