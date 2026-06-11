package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.equipment.CreateEquipmentRequest;
import com.example.schedulemeetingbe.dto.request.equipment.UpdateEquipmentRequest;
import com.example.schedulemeetingbe.dto.response.equipment.EquipmentResponse;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IEquipmentService {

    EquipmentResponse create(CreateEquipmentRequest request);

    EquipmentResponse update(Long id, UpdateEquipmentRequest request);

    Map<String, Object> delete(Long id);

    EquipmentResponse getDetail(Long id);

    PageResponse<EquipmentResponse> getAll(Pageable pageable);

    PageResponse<EquipmentResponse> search(String keyword, Pageable pageable);
}
