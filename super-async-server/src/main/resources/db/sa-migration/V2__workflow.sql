CREATE TABLE IF NOT EXISTS workflow_definitions
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    description TEXT,
    dag_json    TEXT         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workflow_instances
(
    id              BIGSERIAL PRIMARY KEY,
    definition_id   BIGINT      NOT NULL,
    status          VARCHAR(20) NOT NULL,
    context_payload TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_workflow_instances_status ON workflow_instances (status);

CREATE TABLE IF NOT EXISTS workflow_node_instances
(
    id                   BIGSERIAL PRIMARY KEY,
    workflow_instance_id BIGINT      NOT NULL,
    node_id              VARCHAR(64) NOT NULL,
    node_name            VARCHAR(128),
    task_type            VARCHAR(64) NOT NULL,
    task_key             VARCHAR(255) UNIQUE,
    payload              TEXT,
    status               VARCHAR(20) NOT NULL,
    retry_count          INT         NOT NULL DEFAULT 0,
    max_retry            INT         NOT NULL DEFAULT 3,
    upstream_nodes       TEXT,
    downstream_nodes     TEXT,
    execute_mode         VARCHAR(10) NOT NULL DEFAULT 'SERIAL',
    task_id              BIGINT,
    result_payload       TEXT,
    error_msg            TEXT,
    started_at           TIMESTAMPTZ,
    completed_at         TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_workflow_node_instances_instance_id ON workflow_node_instances (workflow_instance_id);
CREATE INDEX IF NOT EXISTS idx_workflow_node_instances_task_key ON workflow_node_instances (task_key);
CREATE INDEX IF NOT EXISTS idx_workflow_node_instances_status ON workflow_node_instances (status);
