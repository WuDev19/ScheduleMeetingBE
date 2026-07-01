package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.response.booking.*;
import com.example.schedulemeetingbe.dto.response.booking.booking_overlap.BookingOverlapProjection;
import com.example.schedulemeetingbe.dto.response.booking.booking_overlap.BookingOverlapResponse;
import com.example.schedulemeetingbe.dto.response.booking.booking_summary.BookingSummaryProjection;
import com.example.schedulemeetingbe.dto.response.booking.booking_summary.BookingSummaryResponse;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.Notification;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.utils.TimeUtils;

import java.time.OffsetDateTime;
import java.util.List;

public class BookingMapper {
    private BookingMapper() {
    }

    public static BookingResponse mapToBookingResponse(Booking booking, User user, Room room) {
        return new BookingResponse(
                booking.getBookingId(),
                booking.getTitle(),
                booking.getDescription(),
                room.getRoomName(),
                room.getBuilding().getAddress(),
                room.getFloorNumber(),
                user.getFullName(),
                user.getUsername(),
                user.getPhone(),
                user.getEmail(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getAttendeeCount(),
                booking.getCreatedAt(),
                booking.getStatus()
        );
    }

    public static StatusBookingResponse mapToStatusBookingResponse(Booking booking) {
        return new StatusBookingResponse(
                booking.getBookingId(),
                booking.getStatus(),
                booking.getUpdatedAt()
        );
    }

    public static BookingDetailResponse mapToBookingDetailResponse(
            Booking booking,
            User user,
            Room room,
            List<BookingDetailEquipmentResponse> equipments
    ) {
        return new BookingDetailResponse(
                booking.getBookingId(),
                booking.getTitle(),
                booking.getDescription(),
                room.getRoomId(),
                room.getRoomName(),
                room.getBuilding().getAddress(),
                room.getFloorNumber(),
                user.getFullName(),
                user.getUsername(),
                user.getPhone(),
                user.getEmail(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getAttendeeCount(),
                booking.getCreatedAt(),
                equipments,
                booking.getStatus()
        );
    }

    public static BookingSummaryResponse mapToBookingSummaryResponse(BookingSummaryProjection projection) {
        OffsetDateTime startTime = projection.getStartTime().atOffset(TimeUtils.ZONE_OFFSET);
        OffsetDateTime endTime = projection.getEndTime().atOffset(TimeUtils.ZONE_OFFSET);
        return new BookingSummaryResponse(
                projection.getBookingId(),
                projection.getHistoryId(),
                projection.getTitle(),
                projection.getUserBooked(),
                projection.getPhone(),
                projection.getRoomName(),
                projection.getStatus(),
                projection.getActionType(),
                startTime,
                endTime
        );
    }

    public static BookingNotificationResponse mapToBookingNotificationResponse(
            Booking booking,
            Room room,
            User user,
            Notification notification
    ) {
        return new BookingNotificationResponse(
                booking.getBookingId(),
                booking.getTitle(),
                booking.getDescription(),
                room.getRoomId(),
                room.getRoomName(),
                room.getBuilding().getAddress(),
                room.getFloorNumber(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getAttendeeCount(),
                booking.getCreatedAt(),
                notification.getTitle(),
                notification.getMessage()
        );
    }

    public static BookingOverlapResponse mapToBookingOverlapResponse(BookingOverlapProjection projection){
        OffsetDateTime startTime = projection.getStartTime().atOffset(TimeUtils.ZONE_OFFSET);
        OffsetDateTime endTime = projection.getEndTime().atOffset(TimeUtils.ZONE_OFFSET);
        return new BookingOverlapResponse(
                projection.getBookingId(),
                projection.getTitle(),
                projection.getUserBooked(),
                projection.getPhone(),
                projection.getRoomName(),
                projection.getStatus(),
                startTime,
                endTime
        );
    }

}
