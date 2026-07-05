package com.example.schedulemeetingbe.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DepartmentImportRow {

    @ExcelProperty(index = 0)
    private String departmentName;

    @ExcelProperty(index = 1)
    private String departmentCode;

    @ExcelProperty(index = 2)
    private String description;
}
