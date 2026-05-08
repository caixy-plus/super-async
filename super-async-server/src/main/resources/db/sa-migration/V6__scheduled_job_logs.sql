CREATE TABLE IF NOT EXISTS scheduled_job_logs
(
    id               BIGSERIAL PRIMARY KEY,
    scheduled_job_id BIGINT,
    level            VARCHAR(10)  NOT NULL, -- INFO, WARN, ERROR
    message          TEXT         NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_scheduled_job_logs_job_created ON scheduled_job_logs (scheduled_job_id, created_at DESC);
