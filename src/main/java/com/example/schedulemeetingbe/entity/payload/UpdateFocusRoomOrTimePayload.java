package com.example.schedulemeetingbe.entity.payload;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

// payload này dùng để hiển thị khi xem chi tiết thay đổi lúc approver duyệt
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateFocusRoomOrTimePayload{
    private Long bookingId;
    private String title;
    private String description;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Integer attendeeCount;
    private BookingStatus status;
    private String cancellationReason;
    private Long roomId;
    private Long bookedBy;
    private OffsetDateTime createdAt;
    private List<String> emails;
}
