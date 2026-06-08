package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.unavailability_room.CreateUnavailabilityRoomRequest;
import com.example.schedulemeetingbe.dto.request.unavailability_room.UpdateUnavailabilityRoomRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.UnavailabilityRoomResponse;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.RoomUnavailability;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.mapper.RoomMapper;
import com.example.schedulemeetingbe.repository.RoomRepository;
import com.example.schedulemeetingbe.repository.UnavailabilityRoomRepository;
import com.example.schedulemeetingbe.service.base.IUnavailabilityRoomService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnavailabilityRoomServiceImpl implements IUnavailabilityRoomService {

    private final UnavailabilityRoomRepository unavailabilityRoomRepository;
    private final RoomRepository roomRepository;

    @Transactional
    @Override
    public UnavailabilityRoomResponse create(CreateUnavailabilityRoomRequest request) {
        Room room = roomRepository.findById(request.roomId()).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        RoomUnavailability roomUnavailability = RoomUnavailability.builder()
                .room(room)
                .reason(request.reason())
                .startTime(TimeUtils.fromLongToZoneDateTime(request.startTime()))
                .endTime(TimeUtils.fromLongToZoneDateTime(request.endTime()))
                .build();
        RoomUnavailability saved = unavailabilityRoomRepository.save(roomUnavailability);
        return RoomMapper.mapToUnavailabilityRoomResponse(saved);
    }

    @Transactional
    @Override
    public UnavailabilityRoomResponse update(Long id, UpdateUnavailabilityRoomRequest request) {
        RoomUnavailability roomUnavailability = unavailabilityRoomRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (request.reason() != null) {
            roomUnavailability.setReason(request.reason());
        }
        if (request.startTime() != null) {
            roomUnavailability.setStartTime(TimeUtils.fromLongToZoneDateTime(request.startTime()));
        }
        if (request.endTime() != null) {
            roomUnavailability.setEndTime(TimeUtils.fromLongToZoneDateTime(request.endTime()));
        }
        return RoomMapper.mapToUnavailabilityRoomResponse(roomUnavailability);
    }

    @Transactional
    @Override
    public Map<String, Object> delete(Long id) {
        boolean exist = unavailabilityRoomRepository.existsById(id);
        if (!exist) {
            throw new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND);
        }
        unavailabilityRoomRepository.deleteById(id);
        return CRUDResponseHelper.deleteSuccess();
    }

    @Transactional
    @Override
    public Map<String, Object> softDelete(Long id) {
        RoomUnavailability roomUnavailability = unavailabilityRoomRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        roomUnavailability.setIsDeleted(true);
        return CRUDResponseHelper.deleteSuccess();
    }

    @Override
    public UnavailabilityRoomResponse getDetail(Long id) {
        RoomUnavailability roomUnavailability = unavailabilityRoomRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        return RoomMapper.mapToUnavailabilityRoomResponse(roomUnavailability);
    }

    @Override
    public PageResponse<UnavailabilityRoomResponse> getAll(Pageable pageable) {
        Page<RoomUnavailability> page = unavailabilityRoomRepository.findAll(pageable);
        return new PageResponse<>(
                page.getNumber(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getContent().stream()
                        .map(RoomMapper::mapToUnavailabilityRoomResponse)
                        .toList()
        );
    }

    @Override
    public PageResponse<UnavailabilityRoomResponse> search(String keyword, Pageable pageable) {
        Page<RoomUnavailability> roomPage = unavailabilityRoomRepository.findByReasonContainingIgnoreCase(
                keyword,
                pageable
        );
        return new PageResponse<>(
                roomPage.getNumber(),
                roomPage.getNumberOfElements(),
                roomPage.getTotalElements(),
                roomPage.getTotalPages(),
                roomPage.getContent().stream()
                        .map(RoomMapper::mapToUnavailabilityRoomResponse)
                        .toList()
        );
    }

}
