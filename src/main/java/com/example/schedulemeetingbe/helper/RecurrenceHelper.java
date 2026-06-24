package com.example.schedulemeetingbe.helper;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.constant.enums.EVENT_TYPE;
import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.dto.request.recurrence.ApproveRejectRecurringRequest;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.payload.ApproveRejectRecurrencePayload;
import com.example.schedulemeetingbe.exception.custom_exception.OverlapBookingException;
import com.example.schedulemeetingbe.repository.BookingRepository;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RecurrenceHelper {
    private RecurrenceHelper() {
    }

    public static boolean checkLimitRecurrence(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) >= 30;
    }

    public static void validateAndSaveBooking(Room room, List<Booking> bookings, List<String> rangesTime, BookingRepository bookingRepository) {
        List<String> reasons = bookingRepository.checkOverlap(room.getRoomId(), rangesTime.toArray(new String[0]));
        if (!reasons.isEmpty()) {
            throw new OverlapBookingException(reasons);
        }
        bookingRepository.saveAll(bookings);
    }

    public static void createNotificationAndOutboxEvent(
            ApproveRejectRecurringRequest request,
            RecurringPattern recurringPattern,
            INotificationService iNotificationService,
            JsonMapper jsonMapper,
            OutboxEventRepository outboxEventRepository
    ) {
        String message = "Lich họp định kì bắt đầu vào " +
                recurringPattern.getStartDate() +
                " và kết thúc lúc " +
                recurringPattern.getEndDate();
        if (request.status() == BookingStatus.APPROVED) {
            message += " đã được chấp thuân";
            createPayloadAndOutbox(recurringPattern, iNotificationService, jsonMapper, outboxEventRepository, message, recurringPattern.getCreatedBy());
        } else if (request.status() == BookingStatus.REJECTED) {
            message += " không được chấp thuận vì lý do " +
                    request.reason() +
                    ". Vui lòng tạo mới hoặc cập nhật lại thông tin!";
            createPayloadAndOutbox(recurringPattern, iNotificationService, jsonMapper, outboxEventRepository, message, recurringPattern.getCreatedBy());
        }
    }

    private static void createPayloadAndOutbox(
            RecurringPattern recurringPattern,
            INotificationService iNotificationService,
            JsonMapper jsonMapper,
            OutboxEventRepository outboxEventRepository,
            String message,
            User createdBy
    ) {
        Notification notification = iNotificationService.save(StringCommon.TITLE_NOTIFICATION_EMAIL, message, createdBy, null);
        ApproveRejectRecurrencePayload payload = new ApproveRejectRecurrencePayload(
                recurringPattern.getCreatedBy().getEmail(),
                notification.getTitle(),
                notification.getMessage()
        );
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .payload(jsonMapper.valueToTree(payload))
                .eventType(EVENT_TYPE.SEND_EMAIL_APPROVE_REJECT.name())
                .status(OutboxStatus.PENDING)
                .build();
        outboxEventRepository.save(outboxEvent);
    }
}
