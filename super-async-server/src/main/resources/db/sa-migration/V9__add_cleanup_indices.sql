-- 为数据清理优化添加索引
CREATE INDEX IF NOT EXISTS idx_async_tasks_status_created_at ON async_tasks (status, created_at);
