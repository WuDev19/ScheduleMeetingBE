package com.example.schedulemeetingbe.dto.request.booking;

import java.util.List;

public record AddParticipantRequest(
    Long departmentId,
    List<String> emails
) {
}
