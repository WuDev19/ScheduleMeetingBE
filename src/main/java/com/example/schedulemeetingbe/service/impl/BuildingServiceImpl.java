package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.building.BuildingResponse;
import com.example.schedulemeetingbe.dto.request.building.CreateBuildingRequest;
import com.example.schedulemeetingbe.dto.request.building.UpdateBuildingRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.entity.Building;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.mapper.BuildingMapper;
import com.example.schedulemeetingbe.repository.BuildingRepository;
import com.example.schedulemeetingbe.service.base.IBuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static java.lang.Math.max;

@Service
@RequiredArgsConstructor
public class BuildingServiceImpl implements IBuildingService {

    private final BuildingRepository buildingRepository;

    @Override
    public BuildingResponse createBuilding(CreateBuildingRequest request) {
        Building building = Building.builder()
                .buildingName(request.buildingName())
                .address(request.buildingAddress())
                .build();
        Building saved = buildingRepository.save(building);
        return BuildingMapper.mapToBuildingResponse(saved);
    }

    @CacheEvict(value = "building-detail", key = "#id")
    @Transactional
    @Override
    public BuildingResponse updateBuilding(Long id, UpdateBuildingRequest request) {
        Building building = buildingRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (request.buildingName() != null) {
            building.setBuildingName(request.buildingName());
        }
        if (request.buildingAddress() != null) {
            building.setAddress(request.buildingAddress());
        }
        return BuildingMapper.mapToBuildingResponse(building);
    }

    @Override
    public Map<String, Object> deleteBuilding(Long id) {
        boolean check = buildingRepository.existsById(id);
        if (!check) {
            throw new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND);
        }
        buildingRepository.deleteById(id);
        return CRUDResponseHelper.deleteSuccess();
    }

    @Override
    public PageResponse<BuildingResponse> getBuildings(int page, int size) {
        page = max(page, 0);
        size = max(size, 10);
        Pageable pageable = PageRequest.of(page, size);
        Page<Building> buildingPage = buildingRepository.findAll(pageable);
        return new PageResponse<>(
                buildingPage.getNumber(),
                buildingPage.getNumberOfElements(),
                buildingPage.getTotalElements(),
                buildingPage.getTotalPages(),
                buildingPage.getContent()
                        .stream()
                        .map(BuildingMapper::mapToBuildingResponse)
                        .toList()
        );
    }

    @Cacheable(value = "building-search",
            key = "T(java.util.Objects).requireNonNullElse(#keyword, '') + '_' + #page + '_' + #size")
    @Override
    public PageResponse<BuildingResponse> search(String keyword, int page, int size) {
        page = max(page, 0);
        size = max(size, 10);
        Pageable pageable = PageRequest.of(page, size);
        Page<Building> buildingPage = buildingRepository.search(keyword, pageable);
        return new PageResponse<>(
                buildingPage.getNumber(),
                buildingPage.getNumberOfElements(),
                buildingPage.getTotalElements(),
                buildingPage.getTotalPages(),
                buildingPage.getContent()
                        .stream()
                        .map(BuildingMapper::mapToBuildingResponse)
                        .toList()
        );
    }

    @Cacheable(value = "building-detail", key = "#id")
    @Override
    public BuildingResponse getDetail(Long id) {
        Building building = buildingRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        return BuildingMapper.mapToBuildingResponse(building);
    }

}
