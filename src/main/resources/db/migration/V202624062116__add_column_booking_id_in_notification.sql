ALTER TABLE notifications
    ADD COLUMN booking_id BIGINT REFERENCES bookings (booking_id) ON DELETE SET NULL;