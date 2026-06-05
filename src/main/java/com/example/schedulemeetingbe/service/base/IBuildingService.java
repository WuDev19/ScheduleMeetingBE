package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.response.BuildingResponse;
import com.example.schedulemeetingbe.dto.request.building.CreateBuildingRequest;
import com.example.schedulemeetingbe.dto.request.building.UpdateBuildingRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;

import java.util.Map;

public interface IBuildingService {

    BuildingResponse createBuilding(CreateBuildingRequest request);

    BuildingResponse updateBuilding(Long id, UpdateBuildingRequest request);

    Map<String, Object> deleteBuilding(Long id);

    PageResponse<BuildingResponse> getBuildings(int page, int size);

    PageResponse<BuildingResponse> search(String keyword, int page, int size);

    BuildingResponse getDetail(Long id);

}
