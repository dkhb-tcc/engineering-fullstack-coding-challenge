CREATE TABLE IF NOT EXISTS vital_sign (
    id          BIGINT PRIMARY KEY,
    patient_id  BIGINT NOT NULL,
    measured_at BIGINT NOT NULL,
    heart_rate  INT    NOT NULL
);
