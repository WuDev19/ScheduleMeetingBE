INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code = 'BOOKING:EXPORT'
WHERE r.role_name IN ('REGISTER', 'APPROVER');