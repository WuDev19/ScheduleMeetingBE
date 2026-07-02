ALTER TABLE recurring_patterns
ADD COLUMN start_date DATE NOT NULL;

ALTER TABLE recurring_patterns
ALTER COLUMN end_date SET NOT NULL;