package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.entity.Booking;

import java.util.List;

public interface IExcelService {

    /**
     * Xuất danh sách lịch họp ra file Excel (.xlsx) sử dụng EasyExcel.
     *
     * @param bookings danh sách booking đã được query sẵn
     * @return mảng byte của file .xlsx
     */
    byte[] exportBookings(List<Booking> bookings);
}
