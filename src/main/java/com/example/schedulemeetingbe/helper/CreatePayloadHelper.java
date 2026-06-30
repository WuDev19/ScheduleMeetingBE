package com.example.schedulemeetingbe.helper;

import com.example.schedulemeetingbe.dto.request.booking.CreateBookingEquipmentRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.payload.CreateBookingPayload;
import com.example.schedulemeetingbe.entity.payload.UpdateBookingChangePayload;
import com.example.schedulemeetingbe.entity.payload.UpdateFocusRoomOrTimePayload;

import java.util.List;

public class CreatePayloadHelper {

    private CreatePayloadHelper() {
    }

    public static UpdateBookingChangePayload create(Booking booking, Long userId, Long roomId) {
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

    public static CreateBookingPayload create(
            Booking booking,
            Long userId,
            Long roomId,
            List<String> emails,
            List<CreateBookingEquipmentRequest> equipments) {
        return new CreateBookingPayload(
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
                booking.getCreatedAt(),
                emails,
                equipments
        );
    }

    public static UpdateFocusRoomOrTimePayload createUpdateRoomOrTime(Booking booking, Long userId, Long roomId, List<String> emails) {
        return new UpdateFocusRoomOrTimePayload(
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
                booking.getCreatedAt(),
                emails
        );
    }

}
