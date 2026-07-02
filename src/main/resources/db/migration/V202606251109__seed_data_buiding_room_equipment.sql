-- ============================================================
-- BUILDINGS
-- ============================================================

INSERT INTO buildings (building_name, address)
VALUES ('Head Office', '123 Nguyen Trai, Thanh Xuan, Ha Noi'),
       ('Innovation Center', '456 Duy Tan, Cau Giay, Ha Noi'),
       ('Branch Office HCM', '789 Nguyen Van Linh, District 7, Ho Chi Minh City');

-- ============================================================
-- ROOMS
-- ============================================================

INSERT INTO rooms (building_id,
                   room_name,
                   capacity,
                   floor_number,
                   description)
VALUES

-- Head Office
(1, 'Meeting Room A101', 8, 1, 'Small meeting room for team discussions'),
(1, 'Meeting Room A201', 12, 2, 'Standard meeting room'),
(1, 'Conference Room A501', 30, 5, 'Large conference room'),
(1, 'Board Room A801', 20, 8, 'Executive board meetings'),

-- Innovation Center
(2, 'Innovation Lab B101', 15, 1, 'Brainstorming and workshop room'),
(2, 'Meeting Room B201', 10, 2, 'Daily standup and sprint review'),
(2, 'Training Room B401', 40, 4, 'Training and onboarding sessions'),

-- Branch Office HCM
(3, 'Meeting Room H101', 8, 1, 'Client meeting room'),
(3, 'Conference Room H301', 25, 3, 'Regional conference room'),
(3, 'Training Room H501', 35, 5, 'Training room');

-- ============================================================
-- EQUIPMENT
-- ============================================================

INSERT INTO equipment (equipment_name,
                       total_quantity,
                       description)
VALUES ('Projector', 15, 'Portable projector'),
       ('Wireless Microphone', 20, 'Wireless microphone set'),
       ('Speaker System', 10, 'Portable speaker system'),
       ('Video Conference Camera', 12, '4K conference camera'),
       ('Whiteboard', 25, 'Mobile whiteboard'),
       ('HDMI Cable', 50, 'HDMI cable 2m'),
       ('Extension Power Strip', 40, '6-outlet power strip'),
       ('Laptop', 30, 'Company laptop for temporary use'),
       ('TV Display 65 Inch', 8, 'Wall-mounted display'),
       ('Interactive Smart Board', 5, 'Touch-enabled smart board');

-- ============================================================
-- ROOM_EQUIPMENT
-- ============================================================

-- Meeting Room A101
INSERT INTO room_equipment (room_id, equipment_id, quantity)
VALUES (1, 1, 1), -- Projector
       (1, 5, 1), -- Whiteboard
       (1, 6, 2), -- HDMI Cable
       (1, 7, 1);
-- Power Strip

-- Meeting Room A201
INSERT INTO room_equipment (room_id, equipment_id, quantity)
VALUES (2, 1, 1),
       (2, 5, 1),
       (2, 6, 2),
       (2, 7, 2);

-- Conference Room A501
INSERT INTO room_equipment (room_id, equipment_id, quantity)
VALUES (3, 1, 2),
       (3, 2, 4),
       (3, 3, 2),
       (3, 4, 1),
       (3, 9, 2),
       (3, 5, 2);

-- Board Room A801
INSERT INTO room_equipment (room_id, equipment_id, quantity)
VALUES (4, 4, 1),
       (4, 2, 2),
       (4, 9, 1),
       (4, 5, 1);

-- Innovation Lab B101
INSERT INTO room_equipment (room_id, equipment_id, quantity)
VALUES (5, 10, 1),
       (5, 5, 3),
       (5, 6, 2);

-- Meeting Room B201
INSERT INTO room_equipment (room_id, equipment_id, quantity)
VALUES (6, 1, 1),
       (6, 5, 1),
       (6, 6, 1);

-- Training Room B401
INSERT INTO room_equipment (room_id, equipment_id, quantity)
VALUES (7, 1, 2),
       (7, 2, 6),
       (7, 3, 2),
       (7, 4, 2),
       (7, 9, 2),
       (7, 5, 2);

-- Meeting Room H101
INSERT INTO room_equipment (room_id, equipment_id, quantity)
VALUES (8, 1, 1),
       (8, 5, 1),
       (8, 6, 1);

-- Conference Room H301
INSERT INTO room_equipment (room_id, equipment_id, quantity)
VALUES (9, 1, 1),
       (9, 2, 4),
       (9, 3, 2),
       (9, 4, 1),
       (9, 9, 1);

-- Training Room H501
INSERT INTO room_equipment (room_id, equipment_id, quantity)
VALUES (10, 1, 2),
       (10, 2, 4),
       (10, 3, 2),
       (10, 5, 2),
       (10, 9, 2);