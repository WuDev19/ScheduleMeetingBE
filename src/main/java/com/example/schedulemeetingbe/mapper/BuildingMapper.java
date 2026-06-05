package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.dto.response.BuildingResponse;
import com.example.schedulemeetingbe.entity.Building;

public class BuildingMapper {

    private BuildingMapper() {
    }

    public static BuildingResponse mapToBuildingResponse(Building building) {
        return new BuildingResponse(
                building.getBuildingId(),
                building.getBuildingName(),
                building.getAddress()
        );
    }

}
