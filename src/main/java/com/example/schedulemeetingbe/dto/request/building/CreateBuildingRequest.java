package com.example.schedulemeetingbe.dto.request.building;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBuildingRequest(
        @NotNull(message = StringCommon.NOT_NULL + "buildingName")
        @NotBlank(message = "Dữ liệu buildingName " + StringCommon.NOT_BLANK)
        String buildingName,

        @NotNull(message = StringCommon.NOT_NULL + "buildingAddress")
        @NotBlank(message = "Dữ liệu buildingAddress " + StringCommon.NOT_BLANK)
        String buildingAddress
) {
}
