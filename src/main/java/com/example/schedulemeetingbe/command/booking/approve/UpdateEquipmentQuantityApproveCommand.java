package com.example.schedulemeetingbe.command.booking.approve;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.constant.enums.ReservationStatus;
import com.example.schedulemeetingbe.dto.request.booking.ApproveRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.entity.payload.UpdateBookingEquipmentQuantityPayload;
import com.example.schedulemeetingbe.repository.BookingEquipmentReservationRepository;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class UpdateEquipmentQuantityApproveCommand implements BookingApproveCommand {
    private final JsonMapper jsonMapper;
    private final BookingEquipmentReservationRepository bookingEquipmentReservationRepository;

    @Override
    public BookingActionType getActionType() {
        return BookingActionType.UPDATE_EQUIP_QUANTITY;
    }

    @Override
    public void execute(Booking booking, ApproveRequest request, User approver) {
        UpdateBookingEquipmentQuantityPayload newData = jsonMapper.treeToValue(request.newData(), UpdateBookingEquipmentQuantityPayload.class);
        bookingEquipmentReservationRepository.findByBookingEquipment_BookingEquipmentId(newData.beId())
                .ifPresent(reservation -> reservation.setStatus(ReservationStatus.DONE));
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.ZONE_DATE_TIME);
    }
}
