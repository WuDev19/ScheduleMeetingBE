package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.entity.converter.Jackson3JsonParser;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.Getter;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
public class BookingHistoryResponse {

    private final Long bookingId;
    private final String title;
    private final String description;
    private final String roomName;
    private final String roomAddress;
    private final Integer floorNumber;
    private final String userBooked;
    private final String phone;
    private final String email;
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;
    private final Integer attendee;
    private final Long bookingHistoryId;
    private final String userChanged;
    private final BookingActionType actionType;
    private final JsonNode oldData;
    private final JsonNode newData;

    public BookingHistoryResponse(
            Long bookingId,
            String title,
            String description,
            String roomName,
            String roomAddress,
            Integer floorNumber,
            String userBooked,
            String phone,
            String email,
            Instant startTime,
            Instant endTime,
            Integer attendee,
            Long bookingHistoryId,
            String userChanged,
            String actionType,
            String oldDataRaw,
            String newDataRaw
    ) {
        this.bookingId = bookingId;
        this.title = title;
        this.description = description;
        this.roomName = roomName;
        this.roomAddress = roomAddress;
        this.floorNumber = floorNumber;
        this.userBooked = userBooked;
        this.phone = phone;
        this.email = email;
        this.startTime = startTime.atOffset(TimeUtils.ZONE_OFFSET);
        this.endTime = endTime.atOffset(TimeUtils.ZONE_OFFSET);
        this.attendee = attendee;
        this.bookingHistoryId = bookingHistoryId;
        this.userChanged = userChanged;
        this.actionType = BookingActionType.valueOf(actionType);
        this.oldData = Jackson3JsonParser.parse(oldDataRaw);
        this.newData = Jackson3JsonParser.parse(newDataRaw);
    }
}
