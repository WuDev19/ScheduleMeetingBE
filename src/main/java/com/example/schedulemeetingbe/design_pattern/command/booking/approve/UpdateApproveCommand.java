package com.example.schedulemeetingbe.design_pattern.command.booking.approve;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.constant.enums.ReservationStatus;
import com.example.schedulemeetingbe.dto.request.booking.ApproveRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.repository.BookingReservationRepository;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class UpdateApproveCommand extends BookingApproveCommand {
    private final BookingReservationRepository bookingReservationRepository;

    public UpdateApproveCommand(INotificationService iNotificationService,
                                OutboxEventRepository outboxEventRepository,
                                JsonMapper jsonMapper,
                                BookingReservationRepository bookingReservationRepository
    ) {
        super(iNotificationService, outboxEventRepository, jsonMapper);
        this.bookingReservationRepository = bookingReservationRepository;
    }

    @Override
    public BookingActionType getActionType() {
        return BookingActionType.UPDATED;
    }

    @Override
    public void execute(Booking booking, ApproveRequest request, User approver) {
        super.execute(booking, request, approver);
        bookingReservationRepository.findBookingReservationsByBooking_BookingId(booking.getBookingId())
                .ifPresent(bookingReservation ->
                        bookingReservation.setStatus(ReservationStatus.DONE)
                );
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.ZONE_DATE_TIME);
    }
}
