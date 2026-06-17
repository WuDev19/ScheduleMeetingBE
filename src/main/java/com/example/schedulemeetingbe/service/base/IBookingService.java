package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.booking.*;
import com.example.schedulemeetingbe.dto.response.booking.BookingResponse;
import com.example.schedulemeetingbe.dto.response.booking.StatusBookingResponse;

import java.util.List;
import java.util.Map;

/* về đặt phòng thì còn
 * 1. Thêm thiết bị sau khi đặt phòng
 * 2. APPROVER duyệt phòng - done
 * 3. REGISTER hủy đặt phòng - done
 * 4. Đổi phòng khi đẵ đặt lịch - done
 * 5. Lọc, tìm kiếm
 * 6. APRROVER duyệt phòng sau khi người dùng có sự thay đổi (sẽ phải có so sánh trực quan giữa cũ và thay đổi mới)
 * */
public interface IBookingService {
    BookingResponse createBooking(CreateBookingRequest request, String username);

    Map<String, Long> updateBooking(Long bookingId, UpdateBookingRequest request);

    Map<String, Long> addEquipmentBooking(Long bookingId, List<UpdateEquipmentBookingRequest> request);

    StatusBookingResponse approveBooking(Long bookingId, Long userId);

    StatusBookingResponse rejectBooking(Long bookingId, Long userId);

    StatusBookingResponse cancelBooking(Long bookingId, CancelBookingRequest request);
}
