INSERT INTO permissions (permission_code, description)
VALUES ('RECURRING_BOOKING:APPROVE', 'Duyệt lịch định kì'),
       ('RECURRING_BOOKING:VIEW_ALL', 'Xem lịch định kì chưa được duyệt');

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code IN ('RECURRING_BOOKING:APPROVE',
                                       'RECURRING_BOOKING:VIEW_ALL')
WHERE r.role_name = 'APPROVER';