package com.example.schedulemeetingbe.dto.request.equipment;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEquipmentRequest(
        @NotBlank(message = "Dữ liệu equipmentName " + StringCommon.NOT_BLANK)
        String equipmentName,

        String description,

        @Min(0)
        @NotNull(message = StringCommon.NOT_NULL + "availableQuantity")
        Integer availableQuantity
) {
}
