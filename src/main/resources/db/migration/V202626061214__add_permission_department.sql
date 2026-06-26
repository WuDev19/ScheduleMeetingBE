INSERT INTO permissions(permission_code, description)
VALUES ('DEPARTMENT:CREATE', 'Thêm phòng ban'),
       ('DEPARTMENT:VIEW', 'Xem thông tin phòng ban'),
       ('DEPARTMENT:UPDATE', 'Cập nhật phòng ban'),
       ('DEPARTMENT:DELETE', 'Xóa phòng ban');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code IN ('DEPARTMENT:CREATE',
                                       'DEPARTMENT:VIEW',
                                       'DEPARTMENT:UPDATE',
                                       'DEPARTMENT:DELETE'
                  )
WHERE r.role_name = 'ADMIN';