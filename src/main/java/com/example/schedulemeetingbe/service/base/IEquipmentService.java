package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.equipment.CreateEquipmentRequest;
import com.example.schedulemeetingbe.dto.request.equipment.UpdateEquipmentRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.equipment.EquipmentAndQuantityResponse;
import com.example.schedulemeetingbe.dto.response.equipment.EquipmentResponse;
import com.example.schedulemeetingbe.entity.Equipment;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IEquipmentService {

    EquipmentResponse create(CreateEquipmentRequest request);

    EquipmentResponse update(Long id, UpdateEquipmentRequest request);

    Map<String, Object> delete(Long id);

    EquipmentResponse getDetail(Long id);

    PageResponse<EquipmentResponse> getAll(Pageable pageable);

    PageResponse<EquipmentResponse> search(String keyword, Pageable pageable);

    List<Equipment> findEquipmentIn(List<Long> ids);

    List<EquipmentAndQuantityResponse> findEquipmentAndRemainingQuantity(List<Long> eqIds);

    EquipmentAndQuantityResponse findEquipmentAndRemainingQuantity(Long beId);

    Optional<Equipment> getEquipmentDetail(Long id);

    Optional<Equipment> getEquipmentWithLock(Long id);

    void lockEquipment(List<Long> eqIds);
}
