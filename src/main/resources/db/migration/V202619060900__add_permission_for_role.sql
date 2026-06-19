INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code = 'BOOKING_STATUS:VIEW'
WHERE r.role_name = 'APPROVER';

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code IN (
                                       'RECURRING_BOOKING:MANAGE',
                                       'BOOKING_HISTORY:VIEW'
                  )
WHERE r.role_name = 'REGISTER';
