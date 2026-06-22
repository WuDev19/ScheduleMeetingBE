package com.example.schedulemeetingbe.helper;

import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.payload.UpdateBookingChangePayload;

public class CreatePayloadHelper {

    private CreatePayloadHelper(){}

    public static UpdateBookingChangePayload create(Booking booking, Long userId, Long roomId){
        return new UpdateBookingChangePayload(
                booking.getBookingId(),
                booking.getTitle(),
                booking.getDescription(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getAttendeeCount(),
                booking.getStatus(),
                booking.getCancellationReason(),
                roomId,
                userId,
                booking.getCreatedAt()
        );
    }
}
