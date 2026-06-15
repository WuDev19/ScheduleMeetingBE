package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.dto.response.booking.BookingResponse;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.User;

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
}
