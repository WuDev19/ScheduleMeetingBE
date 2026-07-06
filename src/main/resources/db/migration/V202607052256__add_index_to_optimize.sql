--bảng booking history
CREATE INDEX idx_booking_history_latest
    ON booking_history
        (
         booking_id,
         created_at DESC
            ) WHERE
is_revoked = false
AND action_type IN
(
'UPDATED',
'ADD_EQUIPMENT',
'UPDATE_EQUIP_QUANTITY',
'CREATED'
);

--bảng booking
DROP INDEX idx_bookings_start_status;

CREATE INDEX idx_bookings_status_approved
    ON bookings (status);

--booking reservation
CREATE INDEX idx_booking_reservation_room_time_gist
    ON booking_reservation USING GIST (old_room_id, tstzrange(old_start_time, old_end_time))
    WHERE status = 'AWAIT_APPROVE';

--bảng booking_equipment
CREATE INDEX idx_equipment_id ON booking_equipment(equipment_id);

--bảng outbox_events
CREATE INDEX idx_status ON outbox_events(status);

--bảng recurring_patterns
CREATE INDEX idx_user_id_status ON recurring_patterns(created_by, status);

--bảng users
CREATE INDEX idx_fullname_start ON users(full_name);

--bảng verification_tokens
CREATE INDEX idx_user_id_is_revoked ON verification_tokens(user_id, revoked);




