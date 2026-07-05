package com.example.schedulemeetingbe.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.schedulemeetingbe.dto.excel.DepartmentImportRow;
import com.example.schedulemeetingbe.entity.Department;
import com.example.schedulemeetingbe.repository.DepartmentRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EasyExcel ReadListener cho việc import phòng ban từ file Excel.
 * Mỗi lần đọc 1 row sẽ validate ngay tại chỗ, hợp lệ thì thu thập vào danh sách,
 * không hợp lệ thì ghi lỗi kèm số dòng.
 * <p>
 * Lưu ý: Listener KHÔNG được khai báo là Spring Bean (@Component) vì EasyExcel
 * tạo instance mới mỗi lần parse — inject thủ công qua constructor.
 */
@Slf4j
@Getter
public class DepartmentImportListener extends AnalysisEventListener<DepartmentImportRow> {

    private final DepartmentRepository departmentRepository;
    private final List<Department> validDepartments = new ArrayList<>();
    private final List<Map<String, String>> errors = new ArrayList<>();

    // Theo dõi trùng lặp nội bộ trong file (không cần query DB thêm lần nữa)
    private final Map<String, Integer> seenCodes = new HashMap<>();
    private final Map<String, Integer> seenNames = new HashMap<>();

    // EasyExcel truyền số dòng bắt đầu từ 0 (không tính header), ta cộng 2 để khớp với Excel
    private int rowIndex = 2;

    public DepartmentImportListener(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void invoke(DepartmentImportRow row, AnalysisContext context) {
        int currentRow = rowIndex++;

        String departmentName = row.getDepartmentName() != null ? row.getDepartmentName().trim() : "";
        String departmentCode = row.getDepartmentCode() != null ? row.getDepartmentCode().trim() : "";
        String description   = row.getDescription()   != null ? row.getDescription().trim()   : null;

        // Bỏ qua dòng hoàn toàn trống
        if (departmentName.isBlank() && departmentCode.isBlank()) {
            return;
        }

        if (departmentName.isBlank()) {
            errors.add(Map.of("row", String.valueOf(currentRow), "error", "Tên phòng ban không được để trống"));
            return;
        }
        if (departmentCode.isBlank()) {
            errors.add(Map.of("row", String.valueOf(currentRow), "error", "Mã phòng ban không được để trống"));
            return;
        }

        String codeKey = departmentCode.toLowerCase();
        String nameKey = departmentName.toLowerCase();

        if (seenCodes.containsKey(codeKey)) {
            errors.add(Map.of("row", String.valueOf(currentRow), "error", "Mã phòng ban bị trùng trong file"));
            return;
        }
        if (seenNames.containsKey(nameKey)) {
            errors.add(Map.of("row", String.valueOf(currentRow), "error", "Tên phòng ban bị trùng trong file"));
            return;
        }
        if (departmentRepository.existsByDepartmentCode(departmentCode)) {
            errors.add(Map.of("row", String.valueOf(currentRow), "error", "Mã phòng ban đã tồn tại"));
            return;
        }
        if (departmentRepository.existsByDepartmentName(departmentName)) {
            errors.add(Map.of("row", String.valueOf(currentRow), "error", "Tên phòng ban đã tồn tại"));
            return;
        }

        seenCodes.put(codeKey, currentRow);
        seenNames.put(nameKey, currentRow);
        validDepartments.add(Department.builder()
                .departmentName(departmentName)
                .departmentCode(departmentCode)
                .description(description)
                .build());
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Import phòng ban hoàn tất: {} hợp lệ, {} lỗi", validDepartments.size(), errors.size());
    }
}
