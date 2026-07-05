package com.example.schedulemeetingbe.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.excel.BookingExcelRow;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.service.base.IExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements IExcelService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT);

    @Override
    public byte[] exportBookings(List<Booking> bookings) {
        List<BookingExcelRow> rows = bookings.stream()
                .map(this::toExcelRow)
                .toList();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            EasyExcel.write(outputStream, BookingExcelRow.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("Lịch họp")
                    .doWrite(rows);
            return outputStream.toByteArray();
        } catch (Exception ex) {
            log.error("Lỗi khi xuất file Excel lịch họp", ex);
            throw new BusinessException(ErrorResponse.FILE_ACCESS_ERROR);
        }
    }

    private BookingExcelRow toExcelRow(Booking booking) {
        return BookingExcelRow.builder()
                .bookingId(booking.getBookingId())
                .title(booking.getTitle() != null ? booking.getTitle() : "")
                .description(booking.getDescription() != null ? booking.getDescription() : "")
                .roomName(booking.getRoom().getRoomName())
                .buildingAddress(booking.getRoom().getBuilding().getAddress())
                .floorNumber(booking.getRoom().getFloorNumber())
                .bookedBy(booking.getBookedBy().getFullName())
                .email(booking.getBookedBy().getEmail())
                .phone(booking.getBookedBy().getPhone() != null ? booking.getBookedBy().getPhone() : "")
                .status(booking.getStatus().name())
                .startTime(booking.getStartTime().format(FORMATTER))
                .endTime(booking.getEndTime().format(FORMATTER))
                .attendeeCount(booking.getAttendeeCount())
                .build();
    }
}
