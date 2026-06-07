package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.dto.response.RoomResponse;
import com.example.schedulemeetingbe.dto.response.UnavailabilityRoomResponse;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.RoomUnavailability;

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

    public static UnavailabilityRoomResponse mapToUnavailabilityRoomResponse(RoomUnavailability roomUnavailability) {
        return new UnavailabilityRoomResponse(
                roomUnavailability.getUnavailableId(),
                roomUnavailability.getReason(),
                roomUnavailability.getStartTime(),
                roomUnavailability.getEndTime(),
                mapToRoomResponse(roomUnavailability.getRoom())
        );
    }
}
