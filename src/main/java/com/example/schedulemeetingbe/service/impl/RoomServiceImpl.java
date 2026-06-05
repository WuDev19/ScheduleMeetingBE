package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.request.room.CreateRoomRequest;
import com.example.schedulemeetingbe.dto.response.RoomResponse;
import com.example.schedulemeetingbe.entity.Building;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.mapper.RoomMapper;
import com.example.schedulemeetingbe.repository.BuildingRepository;
import com.example.schedulemeetingbe.repository.RoomRepository;
import com.example.schedulemeetingbe.service.base.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements IRoomService {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;

    @Transactional
    @Override
    public RoomResponse createRoom(CreateRoomRequest request) {
        Building building = buildingRepository.findById(request.buildingId()).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Room room = Room.builder()
                .roomName(request.roomName())
                .capacity(request.capacity())
                .floorNumber(request.floorNumber())
                .description(request.description())
                .building(building)
                .build();
        Room saved = roomRepository.save(room);
        return RoomMapper.mapToRoomResponse(saved);
    }


}
