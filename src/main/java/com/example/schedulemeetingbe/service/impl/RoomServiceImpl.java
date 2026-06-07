package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.room.CreateRoomRequest;
import com.example.schedulemeetingbe.dto.request.room.RoomFilterRequest;
import com.example.schedulemeetingbe.dto.request.room.UpdateRoomRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.RoomResponse;
import com.example.schedulemeetingbe.entity.Building;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.mapper.RoomMapper;
import com.example.schedulemeetingbe.repository.BuildingRepository;
import com.example.schedulemeetingbe.repository.RoomRepository;
import com.example.schedulemeetingbe.repository.specification.RoomSpecification;
import com.example.schedulemeetingbe.service.base.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

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

    @CacheEvict(value = "room-detail", key = "#id")
    @Transactional
    @Override
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (request.roomName() != null) {
            room.setRoomName(request.roomName());
        }
        if (request.capacity() != null) {
            room.setCapacity(request.capacity());
        }
        if (request.floorNumber() != null) {
            room.setFloorNumber(request.floorNumber());
        }
        if (request.description() != null) {
            room.setDescription(request.description());
        }
        return RoomMapper.mapToRoomResponse(room);
    }

    @Transactional
    @CacheEvict(value = "room-detail", key = "#id")
    @Override
    public Map<String, Object> deleteRoom(Long id) {
        boolean exist = roomRepository.existsById(id);
        if (!exist) {
            throw new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND);
        }
        roomRepository.deleteById(id);
        return CRUDResponseHelper.deleteSuccess();
    }

    @Transactional
    @CacheEvict(value = "room-detail", key = "#id")
    @Override
    public Map<String, Object> softDeleteRoom(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        room.setDeletedAt(ZonedDateTime.now(ZoneOffset.UTC));
        return CRUDResponseHelper.deleteSuccess();
    }

    @Override
    public PageResponse<RoomResponse> getRooms(Pageable pageable) {
        Page<Room> roomPage = roomRepository.findByIsActiveIsTrue(pageable);
        return new PageResponse<>(
                roomPage.getNumber(),
                roomPage.getNumberOfElements(),
                roomPage.getTotalElements(),
                roomPage.getTotalPages(),
                roomPage.getContent().stream()
                        .map(RoomMapper::mapToRoomResponse)
                        .toList()
        );
    }

    @Override
    public PageResponse<RoomResponse> filter(RoomFilterRequest request, Pageable pageable) {
        Page<Room> roomPage = roomRepository.findAll(
                RoomSpecification.filter(request.capacity(), request.floorNumber()),
                pageable
        );
        return new PageResponse<>(
                roomPage.getNumber(),
                roomPage.getNumberOfElements(),
                roomPage.getTotalElements(),
                roomPage.getTotalPages(),
                roomPage.getContent().stream()
                        .map(RoomMapper::mapToRoomResponse)
                        .toList()
        );
    }

    @Override
    public PageResponse<RoomResponse> search(String keyword, Pageable pageable) {
        Page<Room> roomPage = roomRepository.findByRoomNameContainingIgnoreCase(keyword, pageable);
        return new PageResponse<>(
                roomPage.getNumber(),
                roomPage.getNumberOfElements(),
                roomPage.getTotalElements(),
                roomPage.getTotalPages(),
                roomPage.getContent().stream()
                        .map(RoomMapper::mapToRoomResponse)
                        .toList()
        );
    }

    @Cacheable(value = "room-detail", key = "#id")
    @Override
    public RoomResponse getDetail(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        return RoomMapper.mapToRoomResponse(room);
    }

}
