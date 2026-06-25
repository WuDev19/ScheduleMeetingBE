package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.equipment.RoomEquipmentQuantityRequest;
import com.example.schedulemeetingbe.dto.request.room.CreateRoomRequest;
import com.example.schedulemeetingbe.dto.request.room.RoomFilterRequest;
import com.example.schedulemeetingbe.dto.request.room.StartEndTimeRequest;
import com.example.schedulemeetingbe.dto.request.room.UpdateRoomRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.RoomResponse;
import com.example.schedulemeetingbe.dto.response.equipment.RoomEquipmentResponse;
import com.example.schedulemeetingbe.entity.Building;
import com.example.schedulemeetingbe.entity.Equipment;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.RoomEquipment;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.mapper.RoomMapper;
import com.example.schedulemeetingbe.repository.BuildingRepository;
import com.example.schedulemeetingbe.repository.EquipmentRepository;
import com.example.schedulemeetingbe.repository.RoomEquipmentRepository;
import com.example.schedulemeetingbe.repository.RoomRepository;
import com.example.schedulemeetingbe.repository.specification.RoomSpecification;
import com.example.schedulemeetingbe.service.base.IRoomService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements IRoomService {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;
    private final RoomEquipmentRepository roomEquipmentRepository;
    private static final String ROOM_ID = "roomId";

    @Transactional
    @Override
    public Map<String, Long> createRoom(CreateRoomRequest request) {
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
        if (request.equipments() != null && !request.equipments().isEmpty()) {
            Map<Long, Equipment> equipments = equipmentRepository.findByEquipmentIdIn(request.equipments()
                            .stream()
                            .map(RoomEquipmentQuantityRequest::equipmentId)
                            .toList()
                    )
                    .stream()
                    .collect(Collectors.toMap(Equipment::getEquipmentId, Function.identity()));
            List<RoomEquipment> roomEquipments = request.equipments().stream()
                    .map(req -> RoomEquipment.builder()
                            .equipment(equipments.get(req.equipmentId()))
                            .room(saved)
                            .quantity(req.quantity())
                            .build())
                    .toList();
            roomEquipmentRepository.saveAll(roomEquipments);
        }
        return Map.of(ROOM_ID, saved.getRoomId());
    }

    @CacheEvict(value = "room-detail", key = "#id")
    @Transactional
    @Override
    public Map<String, Long> updateRoom(Long id, UpdateRoomRequest request) {
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
        return Map.of(ROOM_ID, room.getRoomId());
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
        room.setDeletedAt(TimeUtils.now());
        return CRUDResponseHelper.deleteSuccess();
    }

    @Override
    public PageResponse<RoomResponse> getRooms(Pageable pageable) {
        Page<Room> roomPage = roomRepository.findByIsActiveIsTrue(pageable);
        return getRoomResponsePageResponse(roomPage);
    }

    @Override
    public PageResponse<RoomResponse> filter(RoomFilterRequest request, Pageable pageable) {
        Page<Room> roomPage = roomRepository.findAll(
                RoomSpecification.filter(request.capacity(), request.floorNumber()),
                pageable
        );
        return getRoomResponsePageResponse(roomPage);
    }

    @Override
    public PageResponse<RoomResponse> search(String keyword, Pageable pageable) {
        Page<Room> roomPage = roomRepository.findByRoomNameContainingIgnoreCase(keyword, pageable);
        return getRoomResponsePageResponse(roomPage);
    }

    @NonNull
    private PageResponse<RoomResponse> getRoomResponsePageResponse(Page<Room> roomPage) {
        Map<Long, List<RoomEquipmentResponse>> roomEquipment = roomEquipmentRepository.findEquipmentByRoomId(
                        roomPage.getContent()
                                .stream()
                                .map(Room::getRoomId)
                                .toList()
                )
                .stream()
                .collect(Collectors.groupingBy(RoomEquipmentResponse::roomId));
        return new PageResponse<>(
                roomPage.getNumber(),
                roomPage.getNumberOfElements(),
                roomPage.getTotalElements(),
                roomPage.getTotalPages(),
                roomPage.getContent().stream()
                        .map(room ->
                                RoomMapper.mapToRoomResponse(room, roomEquipment.getOrDefault(room.getRoomId(), List.of())))
                        .toList()
        );
    }

    @Cacheable(value = "room-detail", key = "#id")
    @Override
    public RoomResponse getDetail(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        List<RoomEquipmentResponse> roomEquipment = roomEquipmentRepository.findEquipmentByRoomId(
                List.of(room.getRoomId())
        );
        return RoomMapper.mapToRoomResponse(room, roomEquipment);
    }

    @Transactional
    @Override
    public Map<String, Long> addEquipmentToRoom(Long roomId, List<RoomEquipmentQuantityRequest> requests) {
        Room room = roomRepository.findById(roomId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Map<Long, Equipment> equipments = equipmentRepository.findByEquipmentIdIn(requests
                        .stream()
                        .map(RoomEquipmentQuantityRequest::equipmentId)
                        .toList()
                )
                .stream()
                .collect(Collectors.toMap(Equipment::getEquipmentId, Function.identity()));
        List<RoomEquipment> roomEquipments = requests.stream()
                .map(req -> RoomEquipment.builder()
                        .equipment(equipments.get(req.equipmentId()))
                        .room(room)
                        .quantity(req.quantity())
                        .build())
                .toList();
        roomEquipmentRepository.saveAll(roomEquipments);
        return Map.of(ROOM_ID, roomId);
    }

    @Override
    public PageResponse<RoomResponse> getRoomNotOverlapTime(Long roomId, StartEndTimeRequest request, Pageable pageable) {
        if (request.start().isAfter(request.end())) {
            throw new BusinessException(ErrorResponse.START_END_DATE_ERROR);
        }
        Page<Room> rooms = roomRepository.findRoomNotOverlap(
                roomId,
                request.start(),
                request.end(),
                pageable
        );
        Map<Long, List<RoomEquipmentResponse>> equipments = roomEquipmentRepository.findEquipmentByRoomId(rooms
                        .stream()
                        .map(Room::getRoomId)
                        .toList()
                )
                .stream()
                .collect(Collectors.groupingBy(RoomEquipmentResponse::roomId));
        return new PageResponse<>(
                rooms.getNumber(),
                rooms.getNumberOfElements(),
                rooms.getTotalElements(),
                rooms.getTotalPages(),
                rooms.getContent()
                        .stream()
                        .map(room ->
                                RoomMapper.mapToRoomResponse(room, equipments.getOrDefault(room.getRoomId(), List.of()))
                        )
                        .toList()
        );
    }

    @Override
    public Optional<Room> getRoomDetail(Long id) {
        return roomRepository.findById(id);
    }
}
