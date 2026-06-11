package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.equipment.CreateEquipmentRequest;
import com.example.schedulemeetingbe.dto.request.equipment.UpdateEquipmentRequest;
import com.example.schedulemeetingbe.dto.response.equipment.EquipmentResponse;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.entity.Equipment;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.mapper.EquipmentMapper;
import com.example.schedulemeetingbe.repository.EquipmentRepository;
import com.example.schedulemeetingbe.service.base.IEquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements IEquipmentService {
    private final EquipmentRepository equipmentRepository;

    @Override
    public EquipmentResponse create(CreateEquipmentRequest request) {
        Equipment equipment = Equipment.builder()
                .equipmentName(request.equipmentName())
                .description(request.description())
                .totalQuantity(request.availableQuantity() < 0 ? 0 : request.availableQuantity())
                .build();
        Equipment saved = equipmentRepository.save(equipment);
        return EquipmentMapper.mapToEquipmentResponse(saved);
    }

    @Transactional
    @Override
    @CacheEvict(value = "equipment", key = "#id")
    public EquipmentResponse update(Long id, UpdateEquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (request.description() != null) {
            equipment.setDescription(request.description());
        }
        if (request.equipmentName() != null) {
            equipment.setEquipmentName(request.equipmentName());
        }
        if (request.availableQuantity() != null) {
            equipment.setTotalQuantity(request.availableQuantity());
        }
        return EquipmentMapper.mapToEquipmentResponse(equipment);
    }

    @Transactional
    @Override
    @CacheEvict(value = "equipment", key = "#id")
    public Map<String, Object> delete(Long id) {
        boolean exist = equipmentRepository.existsById(id);
        if (!exist) {
            throw new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND);
        }
        equipmentRepository.deleteById(id);
        return CRUDResponseHelper.deleteSuccess();
    }

    @Cacheable(value = "equipment", key = "#id")
    @Override
    public EquipmentResponse getDetail(Long id) {
        Equipment equipment = equipmentRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        return EquipmentMapper.mapToEquipmentResponse(equipment);
    }

    @Override
    public PageResponse<EquipmentResponse> getAll(Pageable pageable) {
        Page<Equipment> equipmentPage = equipmentRepository.findAll(pageable);
        return new PageResponse<>(
                equipmentPage.getNumber(),
                equipmentPage.getNumberOfElements(),
                equipmentPage.getTotalElements(),
                equipmentPage.getTotalPages(),
                equipmentPage.getContent()
                        .stream()
                        .map(EquipmentMapper::mapToEquipmentResponse)
                        .toList()
        );
    }

    @Override
    public PageResponse<EquipmentResponse> search(String keyword, Pageable pageable) {
        Page<Equipment> equipmentPage = equipmentRepository.findByEquipmentNameContainingIgnoreCase(
                keyword,
                pageable
        );
        return new PageResponse<>(
                equipmentPage.getNumber(),
                equipmentPage.getNumberOfElements(),
                equipmentPage.getTotalElements(),
                equipmentPage.getTotalPages(),
                equipmentPage.getContent()
                        .stream()
                        .map(EquipmentMapper::mapToEquipmentResponse)
                        .toList()
        );
    }
}
