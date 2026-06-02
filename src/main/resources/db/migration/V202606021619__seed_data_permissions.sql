INSERT INTO permissions (permission_code, description)
VALUES

-- USER
('USER:CREATE', 'Tạo người dùng'),
('USER:VIEW', 'Xem người dùng'),
('USER:UPDATE', 'Cập nhật người dùng'),
('USER:DELETE', 'Xóa người dùng'),
('USER:LOCK', 'Khóa tài khoản người dùng'),
('USER:UNLOCK', 'Mở khóa tài khoản người dùng'),

-- ROLE
('ROLE:VIEW', 'Xem vai trò'),
('ROLE:ASSIGN', 'Gán vai trò cho người dùng'),
('ROLE:MANAGE', 'Quản lý vai trò và phân quyền'),

-- ROOM
('ROOM:CREATE', 'Tạo phòng họp'),
('ROOM:VIEW', 'Xem thông tin phòng họp'),
('ROOM:UPDATE', 'Cập nhật phòng họp'),
('ROOM:DELETE', 'Xóa phòng họp'),

-- EQUIPMENT
('EQUIPMENT:CREATE', 'Thêm thiết bị'),
('EQUIPMENT:VIEW', 'Xem thiết bị'),
('EQUIPMENT:UPDATE', 'Cập nhật thiết bị'),
('EQUIPMENT:DELETE', 'Xóa thiết bị'),

-- ROOM EQUIPMENT
('ROOM_EQUIPMENT:MANAGE', 'Quản lý thiết bị phòng họp'),

-- ROOM UNAVAILABILITY
('ROOM_UNAVAILABLE:MANAGE', 'Quản lý thời gian phòng không khả dụng'),

-- BOOKING
('BOOKING:CREATE', 'Đặt phòng họp'),
('BOOKING:VIEW', 'Xem lịch đặt phòng'),
('BOOKING:UPDATE', 'Cập nhật lịch đặt phòng'),
('BOOKING:CANCEL', 'Hủy lịch đặt phòng'),
('BOOKING:SEARCH', 'Tìm kiếm lịch đặt phòng'),

-- BOOKING APPROVAL
('BOOKING:APPROVE', 'Duyệt lịch đặt phòng'),
('BOOKING:REJECT', 'Từ chối lịch đặt phòng'),

-- BOOKING HISTORY
('BOOKING_HISTORY:VIEW', 'Xem lịch sử booking'),

-- RECURRING BOOKING
('RECURRING_BOOKING:MANAGE', 'Quản lý lịch lặp'),

-- BOOKING STATUS
('BOOKING_STATUS:VIEW', 'Xem trạng thái lịch đặt'),

-- CALENDAR
('CALENDAR:VIEW', 'Xem lịch phòng họp'),

-- NOTIFICATION
('NOTIFICATION:SEND', 'Gửi thông báo'),
('NOTIFICATION:VIEW', 'Xem thông báo'),

-- EMAIL
('EMAIL:SEND', 'Gửi email hệ thống'),

-- EXPORT
('BOOKING:EXPORT', 'Xuất danh sách lịch đặt'),

-- AUDIT LOG
('AUDIT_LOG:VIEW', 'Xem nhật ký hệ thống'),

-- DASHBOARD
('DASHBOARD:VIEW', 'Xem dashboard hệ thống'),

-- SYSTEM
('SYSTEM:CONFIG', 'Cấu hình hệ thống'),
('SYSTEM:MANAGE', 'Quản lý toàn bộ hệ thống');