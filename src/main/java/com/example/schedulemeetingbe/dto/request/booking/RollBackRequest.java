package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import tools.jackson.databind.JsonNode;

public record RollBackRequest (
        BookingActionType actionType,
        JsonNode oldPayload,
        JsonNode newPayload
) {
}
