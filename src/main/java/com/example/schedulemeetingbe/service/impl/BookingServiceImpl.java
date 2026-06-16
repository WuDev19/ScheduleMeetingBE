package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.request.booking.CreateBookingEquipmentRequest;
import com.example.schedulemeetingbe.dto.request.booking.CreateBookingRequest;
import com.example.schedulemeetingbe.dto.request.booking.UpdateBookingRequest;
import com.example.schedulemeetingbe.dto.response.booking.BookingResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneOffset;
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

    // chiều code nốt
    @Transactional
    @Override
    public BookingResponse updateBooking(Long id, UpdateBookingRequest request) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (Duration.between(ZonedDateTime.now(ZoneOffset.UTC), booking.getStartTime())
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
        return null;
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
