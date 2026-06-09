-- Tự check với chính nó trước
ALTER TABLE room_unavailability
    ADD CONSTRAINT no_maintenance_overlap EXCLUDE USING gist (
    room_id WITH =,
    tstzrange(start_time, end_time) WITH &&
);

-- Tạo trigger hủy phòng, tạo notification, thêm vào event_box để gửi mail
CREATE
OR REPLACE FUNCTION auto_cancel_bookings_on_maintenance()
RETURNS TRIGGER AS $$
BEGIN
-- Update trạng thái các booking bị ảnh hưởng và trả ra danh sách vừa update (dùng RETURNING)
WITH affected_bookings AS (
UPDATE bookings
SET status              = 'CANCELLED',
    cancellation_reason = CONCAT('Phòng sửa chữa đột xuất: ', NEW.reason),
    cancelled_at        = NOW()
WHERE room_id = NEW.room_id
  AND status NOT IN ('CANCELLED', 'REJECTED', 'COMPLETED')
  AND deleted_at IS NULL
  AND tstzrange(start_time, end_time) && tstzrange(NEW.start_time, NEW.end_time)
        RETURNING booking_id, booked_by, title, start_time, end_time
    ),
-- Insert hàng loạt vào bảng notifications từ danh sách trên
    insert_notifications AS (
INSERT
INTO notifications(user_id, title, message)
SELECT
    booked_by, 'Lịch họp của bạn bị hủy do sự cố phòng đột xuất', CONCAT('Cuộc họp "', title, '" vào lúc ', start_time, ' và kết thúc lúc ', end_time, ' đã bị hủy do phòng bảo trì đột xuất: ', NEW.reason)
FROM affected_bookings
    )

-- Bước 3: Insert hàng loạt vào outbox_events
INSERT
INTO outbox_events(event_id, event_type, payload, status)
SELECT gen_random_uuid(),
       'BOOKING_CANCELLED_BY_MAINTENANCE',
       jsonb_build_object(
               'bookingId', booking_id,
               'userId', booked_by,
               'roomId', NEW.room_id,
               'reason', NEW.reason,
               'startTime', start_time,
               'endTime', end_time
       ),
       'PENDING'
FROM affected_bookings;

RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER trg_auto_cancel_bookings_on_maintenance
    BEFORE INSERT
    ON room_unavailability
    FOR EACH ROW
    EXECUTE FUNCTION auto_cancel_bookings_on_maintenance();