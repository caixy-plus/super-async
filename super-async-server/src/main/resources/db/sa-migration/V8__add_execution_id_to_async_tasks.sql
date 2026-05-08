ALTER TABLE async_tasks ADD COLUMN IF NOT EXISTS execution_id BIGINT;
CREATE INDEX IF NOT EXISTS idx_async_tasks_execution_id ON async_tasks (execution_id);
