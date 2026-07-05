package com.example.schedulemeetingbe.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.enums.BooleanEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@HeadRowHeight(20)
@HeadFontStyle(bold = BooleanEnum.TRUE, fontHeightInPoints = 11)
public class BookingExcelRow {

    @ExcelProperty("Booking ID")
    @ColumnWidth(15)
    private Long bookingId;

    @ExcelProperty("Tiêu đề")
    @ColumnWidth(30)
    private String title;

    @ExcelProperty("Mô tả")
    @ColumnWidth(40)
    private String description;

    @ExcelProperty("Tên phòng")
    @ColumnWidth(25)
    private String roomName;

    @ExcelProperty("Địa chỉ tòa nhà")
    @ColumnWidth(30)
    private String buildingAddress;

    @ExcelProperty("Tầng")
    @ColumnWidth(10)
    private Integer floorNumber;

    @ExcelProperty("Người đặt")
    @ColumnWidth(25)
    private String bookedBy;

    @ExcelProperty("Email")
    @ColumnWidth(30)
    private String email;

    @ExcelProperty("Số điện thoại")
    @ColumnWidth(20)
    private String phone;

    @ExcelProperty("Trạng thái")
    @ColumnWidth(15)
    private String status;

    @ExcelProperty("Thời gian bắt đầu")
    @ColumnWidth(25)
    private String startTime;

    @ExcelProperty("Thời gian kết thúc")
    @ColumnWidth(25)
    private String endTime;

    @ExcelProperty("Số người tham dự")
    @ColumnWidth(20)
    private Integer attendeeCount;
}
