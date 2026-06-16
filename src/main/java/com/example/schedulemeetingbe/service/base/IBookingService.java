package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.booking.CreateBookingRequest;
import com.example.schedulemeetingbe.dto.request.booking.UpdateBookingRequest;
import com.example.schedulemeetingbe.dto.response.booking.BookingResponse;

/* về đặt phòng thì còn
* 1. Thêm thiết bị sau khi đặt phòng
* 2. APPROVER duyệt phòng
* 3. REGISTER hủy đặt phòng
* 4. Đổi phòng khi đẵ đặt lịch
* */
public interface IBookingService {
    BookingResponse createBooking(CreateBookingRequest request, String username);
    BookingResponse updateBooking(Long id, UpdateBookingRequest request);
}
