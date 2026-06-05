package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.dto.response.RoomResponse;
import com.example.schedulemeetingbe.entity.Room;

public class RoomMapper {

    private RoomMapper() {
    }

    public static RoomResponse mapToRoomResponse(Room room) {
        return new RoomResponse(
                room.getRoomId(),
                room.getRoomName(),
                room.getCapacity(),
                room.getFloorNumber(),
                room.getDescription(),
                BuildingMapper.mapToBuildingResponse(room.getBuilding())
        );
    }
}
