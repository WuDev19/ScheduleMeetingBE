package com.example.schedulemeetingbe.dto.request.building;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.NotBlank;

public record CreateBuildingRequest(
        @NotBlank(message = "Dữ liệu buildingName " + StringCommon.NOT_BLANK)
        String buildingName,

        @NotBlank(message = "Dữ liệu buildingAddress " + StringCommon.NOT_BLANK)
        String buildingAddress
) {
}
