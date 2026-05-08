CREATE TABLE IF NOT EXISTS job_executions
(
    id               BIGSERIAL PRIMARY KEY,
    scheduled_job_id BIGINT       NOT NULL,
    trigger_time     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_time       TIMESTAMPTZ,
    end_time         TIMESTAMPTZ,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, SUCCESS, FAIL
    error_msg        TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_job_executions_job_id ON job_executions (scheduled_job_id, trigger_time DESC);

CREATE TABLE IF NOT EXISTS job_execution_logs
(
    id                  BIGSERIAL PRIMARY KEY,
    execution_record_id BIGINT       NOT NULL,
    line_number         INT          NOT NULL,
    level               VARCHAR(10)  NOT NULL, -- INFO, WARN, ERROR
    message             TEXT         NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_job_execution_logs_record_id ON job_execution_logs (execution_record_id, line_number ASC);
