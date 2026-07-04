package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.unavailability_room.CreateUnavailabilityRoomRequest;
import com.example.schedulemeetingbe.dto.request.unavailability_room.UnavailabilityRoomFilterRequest;
import com.example.schedulemeetingbe.dto.request.unavailability_room.UpdateUnavailabilityRoomRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.UnavailabilityRoomResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IUnavailabilityRoomService {

    UnavailabilityRoomResponse create(CreateUnavailabilityRoomRequest request);

    UnavailabilityRoomResponse update(Long id, UpdateUnavailabilityRoomRequest request);

    Map<String, Object> delete(Long id);

    Map<String, Object> softDelete(Long id);

    UnavailabilityRoomResponse getDetail(Long id);

    PageResponse<UnavailabilityRoomResponse> getAll(Boolean isDeleted, Pageable pageable, List<String> roles);

    PageResponse<UnavailabilityRoomResponse> search(Boolean isDeleted, String keyword, Pageable pageable, List<String> roles);

    PageResponse<UnavailabilityRoomResponse> filter(Boolean isDeleted, UnavailabilityRoomFilterRequest request, Pageable pageable, List<String> roles);

}
