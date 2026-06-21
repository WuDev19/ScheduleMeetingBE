-- cho booking_id unique vào bảng này để ghi đè những yêu cầu cập nhật mới về dữ liệu phòng họp và thời gian
-- để truy vết lịch sử thay đổi thì có bảng booking_history roi
ALTER TABLE booking_reservation
    ADD COLUMN booking_id BIGINT UNIQUE REFERENCES bookings (booking_id) ON DELETE CASCADE;

ALTER TABLE booking_history
    ADD COLUMN is_revoked BOOLEAN NOT NULL DEFAULT FALSE;

CREATE
OR REPLACE FUNCTION check_room_conflict_reservation()
RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
IF
EXISTS (
        SELECT 1
        FROM booking_reservation br
        WHERE br.status = 'AWAIT_APPROVE'
        AND br.booking_id <> NEW.booking_id
        AND br.old_room_id = NEW.room_id
        AND tstzrange(
                br.old_start_time,
                br.old_end_time
            )
            &&
            tstzrange(
                NEW.start_time,
                NEW.end_time
            )
    )
    THEN
        RAISE EXCEPTION
            'Room is reserved for rollback';
END IF;

RETURN NEW;
END;
$$;

CREATE TRIGGER trg_check_room_conflict_reservation
    BEFORE INSERT OR
UPDATE
    ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION check_room_conflict_reservation();

