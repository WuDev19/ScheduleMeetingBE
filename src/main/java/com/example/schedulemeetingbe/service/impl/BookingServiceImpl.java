package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.request.booking.CreateBookingEquipmentRequest;
import com.example.schedulemeetingbe.dto.request.booking.CreateBookingRequest;
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
        List<String> reasons = bookingRepository.checkOverlap(
                request.roomId(),
                new String[]{
                        String.format(
                                "[%s, %s)",
                                request.start().toOffsetDateTime(),
                                request.end().toOffsetDateTime()
                        )}
        );
        if (!reasons.isEmpty()) {
            throw new OverlapBookingException(reasons);
        }
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
                if (createBookingEquipmentRequest.quantity() > equipmentAndQuantity.remainingQuantity()) {
                    exceedQuantity.add("Vượt quá số lượng, thiết bị " +
                            equipmentAndQuantity.equipmentName() +
                            " chỉ còn trống " +
                            equipmentAndQuantity.remainingQuantity()
                    );
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
}
