package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.room.CreateRoomRequest;
import com.example.schedulemeetingbe.dto.response.RoomResponse;

public interface IRoomService {
    RoomResponse createRoom(CreateRoomRequest request);
}
