INSERT INTO permissions(permission_code, description)
VALUES ('BOOKING:DELETE', 'Xóa booking');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code = 'BOOKING:DELETE'
WHERE r.role_name = 'ADMIN';