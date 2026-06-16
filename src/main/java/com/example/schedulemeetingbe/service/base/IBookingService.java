package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.booking.CancelBookingRequest;
import com.example.schedulemeetingbe.dto.request.booking.CreateBookingRequest;
import com.example.schedulemeetingbe.dto.request.booking.UpdateBookingRequest;
import com.example.schedulemeetingbe.dto.response.booking.BookingResponse;
import com.example.schedulemeetingbe.dto.response.booking.StatusBookingResponse;

import java.util.Map;

/* về đặt phòng thì còn
 * 1. Thêm thiết bị sau khi đặt phòng
 * 2. APPROVER duyệt phòng - done
 * 3. REGISTER hủy đặt phòng - done
 * 4. Đổi phòng khi đẵ đặt lịch - done
 * 5. Lọc, tìm kiếm
 * */
public interface IBookingService {
    BookingResponse createBooking(CreateBookingRequest request, String username);

    Map<String, Long> updateBooking(Long id, UpdateBookingRequest request);

    StatusBookingResponse approveBooking(Long bookingId, Long userId);

    StatusBookingResponse rejectBooking(Long bookingId, Long userId);

    StatusBookingResponse cancelBooking(Long bookingId, CancelBookingRequest request);
}
