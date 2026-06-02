-- ADD QUYỀN CHO ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id,
       p.permission_id
FROM roles r
         CROSS JOIN permissions p
WHERE r.role_name = 'ADMIN';

-- ADD QUYỀN CHO APPROVER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id,
       p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code IN (
                                       'ROOM:VIEW',
                                       'EQUIPMENT:VIEW',
                                       'BOOKING:VIEW',
                                       'BOOKING:SEARCH',
                                       'BOOKING:APPROVE',
                                       'BOOKING:REJECT',
                                       'BOOKING_HISTORY:VIEW',
                                       'CALENDAR:VIEW',
                                       'NOTIFICATION:VIEW',
                                       'NOTIFICATION:SEND'
                  )
WHERE r.role_name = 'APPROVER';

-- ADD QUYỀN CHO REGISTER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id,
       p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code IN (
                                       'ROOM:VIEW',
                                       'EQUIPMENT:VIEW',
                                       'BOOKING:CREATE',
                                       'BOOKING:VIEW',
                                       'BOOKING:UPDATE',
                                       'BOOKING:CANCEL',
                                       'BOOKING:SEARCH',
                                       'CALENDAR:VIEW',
                                       'NOTIFICATION:VIEW'
                  )
WHERE r.role_name = 'REGISTER';