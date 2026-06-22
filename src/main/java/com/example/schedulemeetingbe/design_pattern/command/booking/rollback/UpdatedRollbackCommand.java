package com.example.schedulemeetingbe.design_pattern.command.booking.rollback;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.constant.enums.ReservationStatus;
import com.example.schedulemeetingbe.dto.request.booking.RollBackRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.repository.BookingReservationRepository;
import com.example.schedulemeetingbe.repository.RoomRepository;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UpdatedRollbackCommand implements BookingRollbackCommand {

    private final JsonMapper jsonMapper;
    private final RoomRepository roomRepository;
    private final BookingReservationRepository bookingReservationRepository;

    @Override
    public BookingActionType getActionType() {
        return BookingActionType.UPDATED;
    }

    @Override
    public void execute(Booking booking, RollBackRequest request, User approver) {
        for (Map.Entry<String, JsonNode> entry : request.newPayload().properties()) {

            String field = entry.getKey();

            JsonNode oldValue = request.oldPayload().get(field);
            JsonNode newValue = entry.getValue();
            if (!Objects.equals(oldValue, newValue)) {
                switch (field) {
                    case "startTime" -> booking.setStartTime(
                            jsonMapper.treeToValue(
                                    oldValue,
                                    ZonedDateTime.class)
                    );
                    case "endTime" -> booking.setEndTime(
                            jsonMapper.treeToValue(
                                    oldValue,
                                    ZonedDateTime.class)
                    );
                    case "roomId" -> {
                        Long roomId = oldValue.asLong();
                        Room room = roomRepository.findById(roomId).orElseThrow(() ->
                                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
                        booking.setRoom(room);
                    }
                }
            }
        }
        bookingReservationRepository.findBookingReservationsByBooking_BookingId(booking.getBookingId())
                .ifPresent(bookingReservation ->
                        bookingReservation.setStatus(ReservationStatus.DONE)
                );

        booking.setStatus(BookingStatus.APPROVED); // approve lại giá trị cũ
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.ZONE_DATE_TIME);
    }
}
