ALTER TABLE rooms ADD CONSTRAINT check_floor CHECK ( floor_number > 0 );
