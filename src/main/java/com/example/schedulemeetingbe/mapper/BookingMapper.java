package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.response.booking.*;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.User;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
                user.getPhone(),
                user.getEmail(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getAttendeeCount(),
                booking.getCreatedAt()
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
                user.getPhone(),
                user.getEmail(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getAttendeeCount(),
                booking.getCreatedAt(),
                equipments
        );
    }

    public static BookingSummaryResponse mapToBookingSummaryResponse(BookingSummaryProjection projection) {
        ZonedDateTime startTime = projection.getStartTime().atZone(ZoneId.of(StringCommon.TIME_ZONE_VN));
        ZonedDateTime endTime = projection.getEndTime().atZone(ZoneId.of(StringCommon.TIME_ZONE_VN));
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

}
