package com.example.schedulemeetingbe.dto.response;

import java.util.UUID;

public record EmailAndTokenResponse(
        String email,
        UUID token
) {
}
