package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.room.CreateRoomRequest;
import com.example.schedulemeetingbe.dto.request.room.RoomFilterRequest;
import com.example.schedulemeetingbe.dto.request.room.UpdateRoomRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.RoomResponse;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IRoomService {
    RoomResponse createRoom(CreateRoomRequest request);

    RoomResponse updateRoom(Long id, UpdateRoomRequest request);

    Map<String, Object> deleteRoom(Long id);

    Map<String, Object> softDeleteRoom(Long id);

    PageResponse<RoomResponse> getRooms(Pageable pageable);

    PageResponse<RoomResponse> filter(RoomFilterRequest request, Pageable pageable);

    PageResponse<RoomResponse> search(String keyword, Pageable pageable);

    RoomResponse getDetail(Long id);
}
