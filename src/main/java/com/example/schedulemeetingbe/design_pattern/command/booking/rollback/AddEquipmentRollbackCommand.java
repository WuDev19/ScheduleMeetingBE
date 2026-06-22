package com.example.schedulemeetingbe.design_pattern.command.booking.rollback;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.request.booking.RollBackRequest;
import com.example.schedulemeetingbe.dto.response.booking.BookingDetailEquipmentResponse;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.entity.payload.AddBookingEquipmentPayload;
import com.example.schedulemeetingbe.repository.BookingEquipmentRepository;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AddEquipmentRollbackCommand implements BookingRollbackCommand {

    private final BookingEquipmentRepository bookingEquipmentRepository;
    private final JsonMapper jsonMapper;

    @Override
    public BookingActionType getActionType() {
        return BookingActionType.ADD_EQUIPMENT;
    }

    @Override
    public void execute(Booking booking, RollBackRequest request, User approver) {
        AddBookingEquipmentPayload oldData = jsonMapper.treeToValue(request.oldPayload(), AddBookingEquipmentPayload.class);
        AddBookingEquipmentPayload newData = jsonMapper.treeToValue(request.newPayload(), AddBookingEquipmentPayload.class);
        List<BookingDetailEquipmentResponse> oldList = oldData.equipments();
        List<BookingDetailEquipmentResponse> newList = newData.equipments();
        List<Long> additionalBookingEquipment = newList.subList(oldList.size(), newList.size())
                .stream()
                .map(BookingDetailEquipmentResponse::bookingEquipmentId)
                .toList();
        bookingEquipmentRepository.deleteAllByIdInBatch(additionalBookingEquipment);

        booking.setStatus(BookingStatus.APPROVED); // approve lại giá trị cũ
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.ZONE_DATE_TIME);
    }

}
