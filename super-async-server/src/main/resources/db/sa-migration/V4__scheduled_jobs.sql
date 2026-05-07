CREATE TABLE IF NOT EXISTS scheduled_jobs
(
    id              BIGSERIAL PRIMARY KEY,
    job_name        VARCHAR(64)  NOT NULL UNIQUE,
    task_type       VARCHAR(64)  NOT NULL,
    task_key        VARCHAR(255) NOT NULL,
    payload         TEXT,
    cron_expression VARCHAR(64)  NOT NULL,
    worker_tag      VARCHAR(64),
    enabled         BOOLEAN      NOT NULL DEFAULT true,
    description     VARCHAR(255),
    last_trigger_at TIMESTAMPTZ,
    next_trigger_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_scheduled_jobs_enabled_next_trigger ON scheduled_jobs (enabled, next_trigger_at);
CREATE INDEX IF NOT EXISTS idx_scheduled_jobs_job_name ON scheduled_jobs (job_name);
