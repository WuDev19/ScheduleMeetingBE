INSERT INTO permissions(permission_code, description)
VALUES ('BUILDING:CREATE', 'Tạo thông tin building'),
       ('BUILDING:VIEW', 'Xem thông tin các tòa nhà'),
       ('BUILDING:UPDATE', 'Cập nhật thông tin tòa nhà'),
       ('BUILDING:DELETE', 'Xóa thông tin tòa nhà');

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code IN (
                                       'BUILDING:CREATE',
                                       'BUILDING:VIEW',
                                       'BUILDING:UPDATE',
                                       'BUILDING:DELETE'
                  )
WHERE r.role_name = 'ADMIN';

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code = 'BUILDING:VIEW'
WHERE r.role_name = 'APPROVER';

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code = 'BUILDING:VIEW'
WHERE r.role_name = 'REGISTER';


