CREATE EXTENSION IF NOT EXISTS btree_gist;

-- DROP TABLES (Ordered by Dependencies)

DROP TABLE IF EXISTS booking_attendees CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS booking_history CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS recurring_patterns CASCADE;
DROP TABLE IF EXISTS room_equipment CASCADE;
DROP TABLE IF EXISTS equipment CASCADE;
DROP TABLE IF EXISTS room_unavailability CASCADE;
DROP TABLE IF EXISTS rooms CASCADE;
DROP TABLE IF EXISTS buildings CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS departments CASCADE;
DROP TYPE IF EXISTS booking_status;
DROP TYPE IF EXISTS recurrence_type;

-- ENUMS

CREATE TYPE booking_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'CANCELLED',
    'COMPLETED'
);

CREATE TYPE recurrence_type AS ENUM (
    'DAILY',
    'WEEKLY',
    'MONTHLY'
);

-- DEPARTMENTS

CREATE TABLE departments
(
    department_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    description     TEXT,

    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

-- ROLES

CREATE TABLE roles
(
    role_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- PERMISSIONS (RBAC)

CREATE TABLE permissions
(
    permission_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    description     TEXT
);

CREATE TABLE role_permissions
(
    role_id       BIGINT NOT NULL REFERENCES roles (role_id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions (permission_id) ON DELETE CASCADE,

    PRIMARY KEY (role_id, permission_id)
);

-- USERS

CREATE TABLE users
(
    user_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username            VARCHAR(50)  NOT NULL UNIQUE,
    email               VARCHAR(150) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    full_name           VARCHAR(100) NOT NULL,
    phone               VARCHAR(20),

    department_id       BIGINT       REFERENCES departments (department_id) ON DELETE SET NULL,

    is_active           BOOLEAN      NOT NULL DEFAULT FALSE,

    failed_login_count  INT          NOT NULL DEFAULT 0,
    locked_until        TIMESTAMPTZ,
    last_login_at       TIMESTAMPTZ,
    password_changed_at TIMESTAMPTZ,

    avatar_url          VARCHAR(500),

    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

-- USER ROLES

CREATE TABLE user_roles
(
    user_id     BIGINT      NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    role_id     BIGINT      NOT NULL REFERENCES roles (role_id) ON DELETE CASCADE,

    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    assigned_by BIGINT      REFERENCES users (user_id) ON DELETE SET NULL,

    PRIMARY KEY (user_id, role_id)
);

-- BUILDINGS

CREATE TABLE buildings
(
    building_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    building_name VARCHAR(100) NOT NULL UNIQUE,
    address       TEXT,

    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMPTZ
);

-- ROOMS

CREATE TABLE rooms
(
    room_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    building_id  BIGINT       NOT NULL REFERENCES buildings (building_id) ON DELETE CASCADE,

    room_name    VARCHAR(100) NOT NULL,
    capacity     INT          NOT NULL CHECK (capacity > 0),

    floor_number INT,
    description  TEXT,

    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ,

    UNIQUE (room_name, building_id)
);

-- EQUIPMENT

CREATE TABLE equipment
(
    equipment_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    equipment_name VARCHAR(100) NOT NULL UNIQUE,
    description    TEXT,

    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- OPTIMIZED: Thêm khóa chính đơn tự tăng giúp tương thích tốt với các ORM (JPA/Hibernate)
CREATE TABLE room_equipment
(
    room_equipment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_id           BIGINT NOT NULL REFERENCES rooms (room_id) ON DELETE CASCADE,
    equipment_id      BIGINT NOT NULL REFERENCES equipment (equipment_id) ON DELETE CASCADE,
    quantity          INT    NOT NULL DEFAULT 1 CHECK (quantity > 0),

    CONSTRAINT uq_room_equipment UNIQUE (room_id, equipment_id)
);

-- RECURRING PATTERNS

CREATE TABLE recurring_patterns
(
    recurring_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    recurrence_type recurrence_type NOT NULL,
    interval_value  INT             NOT NULL DEFAULT 1,
    days_of_week    VARCHAR(50),
    end_date        DATE,
    status          booking_status  NOT NULL DEFAULT 'PENDING',

    created_by      BIGINT          REFERENCES users (user_id) ON DELETE SET NULL,

    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- BOOKINGS

CREATE TABLE bookings
(
    booking_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    room_id             BIGINT         NOT NULL REFERENCES rooms (room_id) ON DELETE RESTRICT,
    booked_by           BIGINT         NOT NULL REFERENCES users (user_id) ON DELETE RESTRICT,

    recurring_id        BIGINT         REFERENCES recurring_patterns (recurring_id) ON DELETE SET NULL,

    title               VARCHAR(255),
    description         TEXT,

    start_time          TIMESTAMPTZ    NOT NULL,
    end_time            TIMESTAMPTZ    NOT NULL,

    attendee_count      INT            NOT NULL DEFAULT 1 CHECK (attendee_count > 0),

    status              booking_status NOT NULL DEFAULT 'PENDING',

    approved_by         BIGINT         REFERENCES users (user_id) ON DELETE SET NULL,
    approved_at         TIMESTAMPTZ,

    cancelled_at        TIMESTAMPTZ,
    cancellation_reason TEXT,

    version             INT            NOT NULL DEFAULT 0,

    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ,

    CHECK (end_time > start_time)
);

-- TRÁNH OVERLAP SỬ DỤNG GIST (index cho kiểu khoảng)
ALTER TABLE bookings
    ADD CONSTRAINT no_room_overlap EXCLUDE USING gist (
    room_id WITH =,
    tstzrange(start_time, end_time) WITH &&
)
WHERE (
    status NOT IN ('CANCELLED', 'REJECTED')
    AND deleted_at IS NULL
);

-- ROOM UNAVAILABILITY

CREATE TABLE room_unavailability
(
    unavailable_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    room_id        BIGINT      NOT NULL REFERENCES rooms (room_id) ON DELETE CASCADE,

    reason         VARCHAR(255),
    start_time     TIMESTAMPTZ NOT NULL,
    end_time       TIMESTAMPTZ NOT NULL,

    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CHECK (end_time > start_time)
);

-- BOOKING ATTENDEES

CREATE TABLE booking_attendees
(
    booking_id BIGINT      NOT NULL REFERENCES bookings (booking_id) ON DELETE CASCADE,
    user_id    BIGINT      NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,

    joined_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (booking_id, user_id)
);

-- BOOKING HISTORY

CREATE TABLE booking_history
(
    history_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    booking_id  BIGINT      NOT NULL REFERENCES bookings (booking_id) ON DELETE CASCADE,

    changed_by  BIGINT      REFERENCES users (user_id) ON DELETE SET NULL,

    action_type VARCHAR(50) NOT NULL,

    old_data    JSONB,
    new_data    JSONB,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- NOTIFICATIONS

CREATE TABLE notifications
(
    notification_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id         BIGINT       NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,

    title           VARCHAR(255) NOT NULL,
    message         TEXT         NOT NULL,

    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_token
(
    refresh_token_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Đổi từ id
    user_id          BIGINT          NOT NULL,
    refresh_token    VARCHAR(500) NOT NULL,
    expire_date      TIMESTAMPTZ    NOT NULL,
    is_revoked       BOOLEAN          NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_rt_token UNIQUE (refresh_token),
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE black_list_access_token
(
    blacklist_token_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Đổi từ id
    token_id           VARCHAR(36) NOT NULL,
    expire_date         TIMESTAMPTZ   NOT NULL,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_bat_token UNIQUE (token_id)
);

CREATE TABLE verification_tokens
(

    verification_token_id UUID PRIMARY KEY,

    user_id               BIGINT       NOT NULL
        REFERENCES users (user_id)
            ON DELETE CASCADE,

    token                 VARCHAR(255) NOT NULL UNIQUE,

    verified              BOOLEAN      NOT NULL DEFAULT FALSE,

    revoked               BOOLEAN      NOT NULL DEFAULT FALSE,

    expires_at            TIMESTAMPTZ  NOT NULL,

    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE outbox_events
(

    event_id     UUID PRIMARY KEY,

    event_type   VARCHAR(100) NOT NULL,

    payload      JSONB        NOT NULL,

    status       VARCHAR(20)  NOT NULL,

    retry_count  INT          NOT NULL DEFAULT 0,

    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    processed_at TIMESTAMPTZ
);

-- INDEXES

CREATE INDEX idx_bookings_room_time
    ON bookings (room_id, start_time, end_time);

CREATE INDEX idx_bookings_user_status
    ON bookings (booked_by, status);

CREATE INDEX idx_bookings_start_status
    ON bookings (start_time, status);

CREATE INDEX idx_notifications_user_read_created
    ON notifications (user_id, is_read, created_at);

CREATE INDEX idx_booking_history_booking
    ON booking_history (booking_id);

CREATE INDEX idx_rooms_building
    ON rooms (building_id);

CREATE INDEX idx_users_department ON users (department_id);
CREATE INDEX idx_bookings_recurring ON bookings (recurring_id);
CREATE INDEX idx_room_equipment_equip ON room_equipment (equipment_id);
CREATE INDEX idx_room_unavailability_room ON room_unavailability (room_id);

-- TRIGGERS & FUNCTIONS

-- 1. UPDATED_AT TRIGGER
CREATE
OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at
= NOW();
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE
    ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_departments_updated_at
    BEFORE UPDATE
    ON departments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_rooms_updated_at
    BEFORE UPDATE
    ON rooms
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_buildings_updated_at
    BEFORE UPDATE
    ON buildings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_bookings_updated_at
    BEFORE UPDATE
    ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 2. ROOM CAPACITY VALIDATION
CREATE
OR REPLACE FUNCTION validate_room_capacity()
RETURNS TRIGGER AS $$
DECLARE room_capacity INT;
BEGIN
SELECT capacity
INTO room_capacity
FROM rooms
WHERE room_id = NEW.room_id;
IF NEW.attendee_count > room_capacity THEN
        RAISE EXCEPTION 'Attendee count (%) exceeds room capacity (%)', NEW.attendee_count, room_capacity;
END IF;

RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_room_capacity
    BEFORE INSERT OR UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION validate_room_capacity();

-- 3. OPTIMISTIC LOCKING HELPER
CREATE
OR REPLACE FUNCTION increment_version()
RETURNS TRIGGER AS $$
BEGIN
    NEW.version
= OLD.version + 1;
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER trg_increment_booking_version
    BEFORE UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION increment_version();

-- 4. OPTIMIZED: TRIGGER KIỂM TRA CHÉO GIỮA BOOKINGS VÀ ROOM UNAVAILABILITY (LỊCH BẢO TRÌ)
CREATE OR REPLACE FUNCTION check_room_unavailability()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status NOT IN ('CANCELLED', 'REJECTED') AND NEW.deleted_at IS NULL THEN
        IF EXISTS (
            SELECT 1
            FROM room_unavailability
            WHERE room_id = NEW.room_id
              AND tstzrange(start_time, end_time) && tstzrange(NEW.start_time, NEW.end_time)
        ) THEN
            RAISE EXCEPTION 'Phòng họp này hiện không khả dụng (đang bảo trì hoặc đóng cửa) trong khung giờ đã chọn!';
        END IF;
    END IF;
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER trg_check_room_unavailability
    BEFORE INSERT OR UPDATE ON bookings
    FOR EACH ROW
    EXECUTE FUNCTION check_room_unavailability();