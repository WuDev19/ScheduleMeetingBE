INSERT INTO permissions(permission_code, description)
VALUES ('RECURRING_BOOKING:VIEW', 'Xem lịch họp định kỳ');

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code IN ('RECURRING_BOOKING:APPROVE',
                                       'RECURRING_BOOKING:VIEW_ALL',
                                       'RECURRING_BOOKING:VIEW')
WHERE r.role_name = 'ADMIN';

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code = 'RECURRING_BOOKING:VIEW'
WHERE r.role_name IN ('APPROVER', 'REGISTER') ON CONFLICT (role_id, permission_id) DO NOTHING;