package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import tools.jackson.databind.JsonNode;

public record RollBackRequest (
        BookingActionType actionType,
        String reason,
        Long historyId,
        JsonNode oldPayload,
        JsonNode newPayload
) {
}
