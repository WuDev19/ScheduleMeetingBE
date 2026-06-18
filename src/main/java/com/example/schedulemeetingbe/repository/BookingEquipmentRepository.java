package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.dto.response.booking.BookingDetailEquipmentResponse;
import com.example.schedulemeetingbe.dto.response.booking.BookingEquipmentResponse;
import com.example.schedulemeetingbe.dto.response.equipment.EquipmentResponse;
import com.example.schedulemeetingbe.entity.BookingEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingEquipmentRepository extends JpaRepository<BookingEquipment, Long> {

    List<BookingEquipment> findBookingEquipmentByBooking_BookingId(Long bookingId);

    void deleteBookingEquipmentsByEquipment_EquipmentIdInAndBooking_BookingId(List<Long> equipmentId, Long bookingId);

    @Query(value = """
            SELECT new com.example.schedulemeetingbe.dto.response.booking.BookingDetailEquipmentResponse(
                be.bookingEquipmentId,
                be.equipment.equipmentId,
                be.equipment.equipmentName,
                be.equipment.description,
                be.quantity
            )
            FROM BookingEquipment be
            WHERE be.booking.bookingId = :bookingId
            """)
    List<BookingDetailEquipmentResponse> getBookingEquipments(@Param("bookingId") Long bookingId);
}
