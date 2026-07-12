package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.equipment.RoomEquipmentQuantityRequest;
import com.example.schedulemeetingbe.dto.request.room.CreateRoomRequest;
import com.example.schedulemeetingbe.dto.request.room.RoomFilterRequest;
import com.example.schedulemeetingbe.dto.request.room.StartEndTimeRequest;
import com.example.schedulemeetingbe.dto.request.room.UpdateRoomRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.RoomResponse;
import com.example.schedulemeetingbe.entity.Room;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IRoomService {
    Map<String, Long> createRoom(CreateRoomRequest request);

    Map<String, Long> updateRoom(Long id, UpdateRoomRequest request);

    Map<String, Object> deleteRoom(Long id);

    Map<String, Object> softDeleteRoom(Long id);

    PageResponse<RoomResponse> getRooms(Pageable pageable);

    PageResponse<RoomResponse> filter(RoomFilterRequest request, Pageable pageable);

    PageResponse<RoomResponse> search(String keyword, Pageable pageable);

    RoomResponse getDetail(Long id);

    Map<String, Long> addEquipmentToRoom(Long roomId, List<RoomEquipmentQuantityRequest> requests);

    Map<String, Object> updateRoomEquipmentQuantity(Long roomId, Long roomEquipmentId, Integer quantity);

    Map<String, Object> deleteRoomEquipment(Long roomId, Long roomEquipmentId);

    PageResponse<RoomResponse> getRoomNotOverlapTime(StartEndTimeRequest request, Pageable pageable);

    Optional<Room> getRoomDetail(Long id);

    void acquireDistributedLockForRoomAndDate(Long roomId, OffsetDateTime dateTime);

    void acquireDistributedLockForRoomAndDate(long[] keys);
}
