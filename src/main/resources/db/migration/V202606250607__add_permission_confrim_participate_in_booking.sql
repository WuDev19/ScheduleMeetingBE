INSERT INTO permissions (permission_code, description)
VALUES ('BOOKING:CONFIRM', 'Xác nhận tham gia lịch họp');

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code = 'BOOKING:CONFIRM'
WHERE r.role_name IN ('REGISTER', 'ADMIN');