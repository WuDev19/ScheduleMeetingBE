package com.example.schedulemeetingbe.design_pattern.command.booking.rollback;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.constant.enums.ReservationStatus;
import com.example.schedulemeetingbe.dto.request.booking.RollBackRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.BookingEquipment;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.entity.payload.UpdateBookingEquipmentQuantityPayload;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.repository.BookingEquipmentRepository;
import com.example.schedulemeetingbe.repository.BookingEquipmentReservationRepository;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class UpdateEquipmentQuantityRollbackCommand extends BookingRollbackCommand {

    private final BookingEquipmentRepository bookingEquipmentRepository;
    private final BookingEquipmentReservationRepository bookingEquipmentReservationRepository;

    public UpdateEquipmentQuantityRollbackCommand(
            INotificationService iNotificationService,
            OutboxEventRepository outboxEventRepository,
            JsonMapper jsonMapper,
            BookingEquipmentRepository bookingEquipmentRepository,
            BookingEquipmentReservationRepository bookingEquipmentReservationRepository
    ) {
        super(iNotificationService, outboxEventRepository, jsonMapper);
        this.bookingEquipmentRepository = bookingEquipmentRepository;
        this.bookingEquipmentReservationRepository = bookingEquipmentReservationRepository;
    }

    @Override
    public BookingActionType getActionType() {
        return BookingActionType.UPDATE_EQUIP_QUANTITY;
    }

    @Override
    public void execute(Booking booking, RollBackRequest request, User approver) {
        super.execute(booking, request, approver);
        UpdateBookingEquipmentQuantityPayload oldData = jsonMapper.treeToValue(request.oldPayload(), UpdateBookingEquipmentQuantityPayload.class);

        bookingEquipmentReservationRepository.findByBookingEquipment_BookingEquipmentId(oldData.beId())
                .ifPresent(reservation -> reservation.setStatus(ReservationStatus.DONE));

        BookingEquipment bookingEquipment = bookingEquipmentRepository.findById(oldData.beId())
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        bookingEquipment.setQuantity(oldData.quantity());

        booking.setStatus(BookingStatus.APPROVED); // approve lại giá trị cũ
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.now());
    }
}
