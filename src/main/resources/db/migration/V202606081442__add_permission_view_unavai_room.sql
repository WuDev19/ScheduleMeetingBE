INSERT INTO permissions(permission_code, description)
VALUES ('ROOM_UNAVAILABLE:VIEW', 'Xem thông tin phòng họp không khả dụng');

INSERT INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
         JOIN permissions p
              ON p.permission_code = 'ROOM_UNAVAILABLE:VIEW';