CREATE TYPE booking_action_type AS ENUM (
    'CREATED',
    'UPDATED',
    'CANCELLED',
    'APPROVED',
    'REJECTED',
    'ADD_EQUIPMENT'
);

ALTER TABLE booking_history
ALTER COLUMN action_type
TYPE booking_action_type
USING action_type::booking_action_type