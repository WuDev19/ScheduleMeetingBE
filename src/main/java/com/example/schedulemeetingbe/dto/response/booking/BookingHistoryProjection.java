package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.entity.converter.Jackson3JsonParser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

public interface BookingHistoryProjection {
    Long getBookingId();

    String getTitle();

    String getDescription();

    String getRoomName();

    String getRoomAddress();

    Integer getFloorNumber();

    String getUserBooked();

    String getPhone();

    String getEmail();

    Instant getStartTime();

    Instant getEndTime();

    Integer getAttendee();

    Long getBookingHistoryId();

    String getUserChanged();

    BookingActionType getActionType();

    String getOldDataRaw();

    String getNewDataRaw();

    @JsonProperty("oldData")
    default JsonNode getOldData() {
        return Jackson3JsonParser.parse(getOldDataRaw());
    }

    @JsonProperty("newData")
    default JsonNode getNewData() {
        return Jackson3JsonParser.parse(getNewDataRaw());
    }

}
