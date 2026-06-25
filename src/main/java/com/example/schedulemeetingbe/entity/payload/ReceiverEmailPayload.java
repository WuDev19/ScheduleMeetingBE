package com.example.schedulemeetingbe.entity.payload;

import java.util.List;

public record ReceiverEmailPayload (
        Long bookingId,
        String title,
        String description,
        String address,
        String room,
        String startTime,
        String endTime,
        List<String> receivers
){
}
