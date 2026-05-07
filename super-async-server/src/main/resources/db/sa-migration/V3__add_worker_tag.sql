ALTER TABLE async_tasks ADD COLUMN IF NOT EXISTS worker_tag VARCHAR(64);
CREATE INDEX IF NOT EXISTS idx_async_tasks_worker_tag ON async_tasks (worker_tag);
