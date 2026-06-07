package com.example.schedulemeetingbe.dto.request.room;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(
        @NotBlank(message = "Dữ liệu roomName " + StringCommon.NOT_BLANK)
        String roomName,

        @NotNull(message = StringCommon.NOT_NULL + "capacity")
        @Min(value = 1, message = "Sức chứa phải lớn hơn hoặc bằng 1")
        Integer capacity,

        @NotNull(message = StringCommon.NOT_NULL + "floorNumber")
        @Min(value = 0)
        Integer floorNumber,

        String description,

        @NotNull(message = StringCommon.NOT_NULL + "buildingId")
        Long buildingId
) {
}
