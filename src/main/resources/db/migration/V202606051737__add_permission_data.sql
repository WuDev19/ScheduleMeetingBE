INSERT INTO permissions(permission_code, description)
VALUES ('USER:VIEW_ALL', 'Xem toàn bộ người dùng');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code = 'USER:VIEW_ALL'
WHERE r.role_name = 'ADMIN';

