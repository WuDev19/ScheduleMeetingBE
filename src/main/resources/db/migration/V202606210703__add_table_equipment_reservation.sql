-- bảo toàn số lượng nếu yêu cầu thay đổi nhỏ hơn với quantity có sẵn để có thể rollback được
-- mà ko bị xảy ra trường hợp khi rollback lại thì bị người khác đặt mất
CREATE TABLE booking_equipment_reservation(
    equipment_reservation_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_equipment_id        BIGINT UNIQUE REFERENCES booking_equipment(booking_equipment_id) ON DELETE CASCADE,
    reservation_quantity        INT,
    status                      reservation_status DEFAULT 'AWAIT_APPROVE',
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);