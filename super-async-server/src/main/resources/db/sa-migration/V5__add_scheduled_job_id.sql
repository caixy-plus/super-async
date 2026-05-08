ALTER TABLE async_tasks ADD COLUMN IF NOT EXISTS scheduled_job_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_async_tasks_scheduled_job_id ON async_tasks (scheduled_job_id);
