CREATE TABLE IF NOT EXISTS async_tasks
(
    id             BIGSERIAL PRIMARY KEY,
    task_type      VARCHAR(64)  NOT NULL,
    task_key       VARCHAR(255) NOT NULL UNIQUE,
    payload        TEXT         NOT NULL,
    priority       INT          NOT NULL DEFAULT 5,
    status         VARCHAR(20)  NOT NULL,
    retry_count    INT          NOT NULL DEFAULT 0,
    max_retry      INT          NOT NULL DEFAULT 3,
    execute_at     TIMESTAMPTZ  NOT NULL,
    timeout_at     TIMESTAMPTZ,
    result_payload TEXT,
    error_msg      TEXT,
    worker_node    VARCHAR(64),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_async_tasks_status_execute_at ON async_tasks (status, execute_at);
CREATE INDEX IF NOT EXISTS idx_async_tasks_status_timeout_at ON async_tasks (status, timeout_at);
CREATE INDEX IF NOT EXISTS idx_async_tasks_task_type ON async_tasks (task_type);
CREATE INDEX IF NOT EXISTS idx_async_tasks_worker_node ON async_tasks (worker_node);
