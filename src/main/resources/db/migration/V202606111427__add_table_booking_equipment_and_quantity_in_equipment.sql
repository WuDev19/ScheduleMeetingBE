ALTER TABLE equipment
    ADD total_quantity INT NOT NULL DEFAULT 0 CHECK (total_quantity >= 0);

CREATE TABLE booking_equipment
(
    booking_equipment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id           BIGINT NOT NULL REFERENCES bookings (booking_id) ON DELETE CASCADE,
    equipment_id         BIGINT NOT NULL REFERENCES equipment (equipment_id) ON DELETE CASCADE,
    quantity             INT    NOT NULL DEFAULT 1 CHECK (quantity > 0),
    CONSTRAINT uq_booking_equipment UNIQUE (booking_id, equipment_id)
);
