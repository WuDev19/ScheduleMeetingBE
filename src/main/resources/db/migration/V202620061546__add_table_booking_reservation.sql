CREATE TYPE reservation_status AS ENUM (
    'AWAIT_APPROVE',
    'DONE'
);

-- để lưu lại giá trị cũ trong thời gian chờ duyệt
CREATE TABLE booking_reservation
(
    reservation_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    old_room_id        BIGINT REFERENCES rooms (room_id) ON DELETE CASCADE,
    old_start_time     TIMESTAMPTZ,
    old_end_time       TIMESTAMPTZ,
    status             reservation_status DEFAULT 'AWAIT_APPROVE',
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
)