INSERT INTO permissions(permission_code, description)
VALUES ('NOTIFICATION:UPDATE', 'Người dùng cập nhật thông báo'),
       ('NOTIFICATION:DELETE', 'Xóa thông báo');

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code IN ('NOTIFICATION:UPDATE',
                                       'NOTIFICATION:DELETE')
WHERE r.role_name IN ('ADMIN', 'APPROVER', 'REGISTER') ON CONFLICT (role_id, permission_id) DO NOTHING;