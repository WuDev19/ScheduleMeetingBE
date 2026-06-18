package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.booking.*;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.booking.*;

import java.util.List;
import java.util.Map;

/* về đặt phòng thì còn
 * 1. Thêm thiết bị sau khi đặt phòng - done
 * 2. APPROVER duyệt phòng - done
 * 3. REGISTER hủy đặt phòng - done
 * 4. Đổi phòng khi đẵ đặt lịch - done
 * 5. Xem chi tiết - done
 * 6. APPROVER duyệt phòng sau khi người dùng có sự thay đổi (sẽ phải có so sánh trực quan giữa cũ và thay đổi mới)
 * 7. Lọc, tìm kiếm (
 * - REGISTER: tìm kiếm các lịch đặt của chính mình
 * - APPROVER: tìm kiếm lọc lịch của tất cả mọi người
 * - ADMIN: toàn quyền
 * )
 * 8. Gửi email cho người tham gia khi đặt lịch họp thành công
 * */
public interface IBookingService {
    BookingResponse createBooking(CreateBookingRequest request, String username);

    Map<String, Long> updateBooking(Long bookingId, UpdateBookingRequest request, Long userId);

    Map<String, Long> addEquipmentBooking(Long bookingId, List<UpdateEquipmentBookingRequest> request, Long userId);

    StatusBookingResponse approveBooking(Long bookingId, Long userId);

    StatusBookingResponse rejectBooking(Long bookingId, Long userId);

    StatusBookingResponse cancelBooking(Long bookingId, CancelBookingRequest request, Long userId);

    Map<String, Object> deleteBooking(Long bookingId, Long userId);

    BookingDetailResponse getBookingDetail(Long bookingId, Long userId);

    BookingEquipmentResponse updateBookingEquipmentQuantity(Long bookingId, Long userId, Long bookingEquipmentId, UpdateBookingEquipQuantityRequest request);

}
