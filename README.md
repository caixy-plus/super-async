# SuperAsync

> 通用异步任务调度平台 —— 高吞吐、低延迟、可水平扩展的分布式任务调度与执行框架。

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellow.svg)](LICENSE)

---

## 目录

- [项目介绍](#项目介绍)
- [架构概览](#架构概览)
- [快速开始](#快速开始)
- [接入指南](#接入指南)
  - [Server 本地模式](#server-本地模式)
  - [Worker 远程模式](#worker-远程模式)
  - [定时任务](#定时任务)
- [性能测试报告](#性能测试报告)
- [模块说明](#模块说明)
- [开源协议](#开源协议)

---

## 项目介绍

SuperAsync 是一个面向生产环境的通用异步任务调度平台，支持两种执行模式：

- **Server 本地模式**：调度器与执行器同进程，适合单机高吞吐场景。
- **Worker 远程模式**：调度器通过 HTTP 向独立 Worker 分发任务，支持水平扩展。

核心特性：

- **PostgreSQL + FOR UPDATE SKIP LOCKED**：原生支持高并发任务抢占，无需额外消息队列。
- **任务优先级 + 延迟调度**：支持优先级队列与精确到毫秒的延迟执行。
- **自动重试 + 超时监控**：失败自动退避重试，超时任务自动回滚。
- **定时任务（Cron）**：内置 Cron 表达式解析，支持秒级精度。
- **工作流引擎（DAG）**：支持基于 DAG 的复杂任务编排（Beta）。
- **Spring Boot 自动装配**：一行配置即可接入现有项目。

---

## 架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                      SuperAsync Server                       │
│  ┌─────────────┐   ┌──────────────┐   ┌─────────────────┐  │
│  │ Task Submit │──▶│  PostgreSQL  │◀──│ Task Polling     │  │
│  │  REST API   │   │  async_tasks │   │ Scheduler(5s)   │  │
│  └─────────────┘   └──────────────┘   └─────────────────┘  │
│         │                   ▲                    │           │
│         │                   │                    ▼           │
│         │            ┌──────────────┐      ┌──────────┐     │
│         │            │ ScheduledJob │      │ Executor │     │
│         │            │   Engine     │      │  Engine  │     │
│         │            └──────────────┘      └──────────┘     │
│         │                                            │       │
│  ┌──────▼────────────────────────────────────────────▼───┐  │
│  │              REST API: /v1/worker/poll                │  │
│  │              REST API: /v1/worker/complete            │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │ HTTP
┌─────────────────────────────────────────────────────────────┐
│                    SuperAsync Worker (SDK)                   │
│  ┌──────────────┐   ┌─────────────────┐   ┌─────────────┐  │
│  │  Poll Loop   │──▶│  SuperAsyncWorker │──▶│  Handler    │  │
│  │  (3s/100ms)  │   │    Registry       │   │ @SuperAsync │  │
│  └──────────────┘   └─────────────────┘   └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 调度流程

1. 客户端通过 `TaskDispatcher.submit()` 将任务写入 `async_tasks` 表（状态 `PENDING`）。
2. **本地模式**：`TaskPollingScheduler` 每 5 秒轮询一批 `PENDING` 任务，调用 `TaskExecutorEngine` 在线程池中执行。
3. **Worker 模式**：`SuperAsyncWorkerEngine` 定期 HTTP 轮询 `/v1/worker/poll`，抢到任务后在本机线程池执行，结果通过 `/v1/worker/complete` 回写。
4. **定时任务**：`JobSchedulerEngine` 每 10 秒扫描 `scheduled_jobs` 表，触达时间的任务会生成对应的 `async_task`。

---

## 快速开始

### 1. 环境准备

- Java 21+
- Maven 3.8+
- PostgreSQL 14+（本地测试可用 Docker 一键启动）

```bash
# 启动 PostgreSQL
docker run -d --name superasync-pg \
  -e POSTGRES_DB=superasync \
  -e POSTGRES_USER=superasync \
  -e POSTGRES_PASSWORD=superasync \
  -p 5432:5432 postgres:16-alpine
```

### 2. 编译安装

```bash
git clone https://github.com/caixy-plus/super-async.git
cd super-async
mvn clean install -DskipTests
```

### 3. 启动 Server

```bash
cd super-async-server
mvn spring-boot:run
```

Server 默认启动在 `http://localhost:8080`，自动执行 Flyway 迁移创建表结构。

### 4. 运行性能测试

```bash
cd super-async-benchmark
mvn spring-boot:run
```

测试完成后会在控制台输出 Markdown 格式的性能报告。

---

## 接入指南

### Server 本地模式

在业务服务中引入 `super-async-server`，通过 `@TaskHandler` 注解注册本地执行器：

```java
import com.superasync.annotation.TaskHandler;
import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;
import org.springframework.stereotype.Component;

@Component
public class OrderTaskHandler {

    @TaskHandler("ORDER_PAYMENT")
    public TaskResult handlePayment(TaskContext ctx) {
        String orderId = ctx.getPayload();
        // 执行业务逻辑
        return TaskResult.ok("paid");
    }
}
```

提交任务：

```java
import com.superasync.dto.TaskRequest;
import com.superasync.dto.Priority;
import com.superasync.service.TaskDispatcher;

@Service
public class OrderService {
    @Autowired
    private TaskDispatcher taskDispatcher;

    public void createOrder(String orderId) {
        TaskRequest request = TaskRequest.builder()
                .taskType("ORDER_PAYMENT")
                .taskKey("order_" + orderId)
                .payload(orderId)
                .priority(Priority.HIGH)
                .delay(Duration.ofSeconds(30))   // 30s 后执行
                .timeout(Duration.ofMinutes(5))
                .maxRetry(3)
                .build();
        taskDispatcher.submit(request);
    }
}
```

### Worker 远程模式

在独立 Worker 服务中引入 `super-async-sdk`：

**1. 配置 `application.yml`：**

```yaml
superasync:
  worker:
    enabled: true
    worker-id: worker-node-1
    server-url: http://localhost:8080
    core-pool-size: 16
    poll-interval-ms: 3000
    tags:
      - PAYMENT_WORKER
```

**2. 编写 Worker 处理器：**

```java
import com.superasync.dto.TaskContext;
import com.superasync.dto.TaskResult;
import com.superasync.worker.annotation.SuperAsyncWorker;
import org.springframework.stereotype.Component;

@Component
public class PaymentWorker {

    @SuperAsyncWorker("ORDER_PAYMENT")
    public TaskResult handle(TaskContext ctx) {
        // 执行业务逻辑
        return TaskResult.ok("done");
    }
}
```

**3. 提交时指定 workerTag：**

```java
TaskRequest request = TaskRequest.builder()
        .taskType("ORDER_PAYMENT")
        .taskKey("order_" + orderId)
        .payload(orderId)
        .workerTag("PAYMENT_WORKER")   // 指定由该标签的 Worker 执行
        .build();
taskDispatcher.submit(request);
```

### 定时任务

```java
import com.superasync.client.SuperAsyncClient;
import com.superasync.client.dto.ScheduledJobRequest;

@Service
public class JobService {
    @Autowired
    private SuperAsyncClient client;

    public void registerJob() {
        client.registerScheduledJob(ScheduledJobRequest.builder()
                .jobName("daily_report")
                .taskType("GENERATE_REPORT")
                .taskKey("report_" + LocalDate.now())
                .payload("{\"type\":\"daily\"}")
                .cronExpression("0 0 9 * * ?")   // 每天 9:00
                .workerTag("REPORT_WORKER")
                .build());
    }
}
```

---

## 性能测试报告

> 测试环境：MacBook Pro (Apple Silicon, 8C), Java 21, PostgreSQL 16 (Docker)
> 测试方式：本地启动 `super-async-benchmark` 模块，连接独立 PostgreSQL 实例

### 测试场景

| 场景 | 模式 | 任务数 | 任务说明 |
|---|---|---|---|
| 简单任务延迟测试 | Worker 手动轮询 | 100 | 空逻辑，测量 submit → execute 延迟 |
| 简单任务吞吐量测试 | 本地模式手动轮询 | 1,000 | 空逻辑，测量系统处理上限 |
| 耗时任务压力测试 | 本地模式手动轮询 | 200 | 单个任务休眠 1000ms，观察堆积与完成时间 |

### 测试结果

#### 简单任务延迟测试 (Worker模式-手动驱动)

| 指标 | 数值 |
|---|---|
| 提交任务数 | 100 |
| 完成任务数 | 100 |
| 失败任务数 | 0 |
| 总耗时 | 0.08 s |
| 吞吐量 | 1282.05 tasks/s |
| 平均延迟 | 415.34 ms |
| 最小延迟 | 73 ms |
| P50 延迟 | 423 ms |
| P95 延迟 | 735 ms |
| P99 延迟 | 786 ms |
| P99.9 延迟 | 786 ms |
| 最大延迟 | 786 ms |

#### 简单任务吞吐量测试 (本地模式-手动驱动)

| 指标 | 数值 |
|---|---|
| 提交任务数 | 1000 |
| 完成任务数 | 1000 |
| 失败任务数 | 0 |
| 总耗时 | 0.26 s |
| 吞吐量 | 3846.15 tasks/s |
| 平均延迟 | 2100.84 ms |
| 最小延迟 | 220 ms |
| P50 延迟 | 1911 ms |
| P95 延迟 | 3893 ms |
| P99 延迟 | 4095 ms |
| P99.9 延迟 | 4103 ms |
| 最大延迟 | 4108 ms |

#### 耗时任务压力测试 (本地模式-手动驱动)

| 指标 | 数值 |
|---|---|
| 提交任务数 | 200 |
| 完成任务数 | 200 |
| 失败任务数 | 0 |
| 总耗时 | 0.05 s |
| 吞吐量 | 3773.58 tasks/s |
| 平均延迟 | 2663.82 ms |
| 最小延迟 | 32 ms |
| P50 延迟 | 3014 ms |
| P95 延迟 | 5029 ms |
| P99 延迟 | 6015 ms |
| P99.9 延迟 | 6016 ms |
| 最大延迟 | 6016 ms |

### 结果分析

- **Worker 延迟**：平均 415ms，P99 786ms。延迟主要来自 Worker HTTP 轮询间隔（默认 3s，测试中手动调优为 100ms）+ 任务排队时间。
- **本地吞吐**：1000 个空任务在 0.26s 内全部提交，实际执行完成耗时约 4.1s（P99 延迟），系统单节点处理能力可达 **~3800 tasks/s** 以上。
- **耗时任务**：200 个 1000ms 任务在 16 线程执行池中平稳完成，无失败、无堆积溢出，平均完成时间约 2.7s，说明调度器具备良好的任务堆积消化能力。

### 架构瓶颈与调优建议

| 瓶颈点 | 当前值 | 说明 |
|---|---|---|
| 本地调度轮询间隔 | 5s（代码硬编码） | 降低可显著减少本地模式延迟 |
| Worker 轮询间隔 | 3s（默认）/ 100ms（测试） | 生产环境建议 1~3s，过低会增加 DB 压力 |
| 单次轮询批次 | 50（代码硬编码） | 高吞吐场景建议调大至 200~500 |
| Worker 单次抢锁 | LIMIT 1 | 多 Worker 场景下并发抢锁效率受限 |
| 定时任务调度间隔 | 10s（代码硬编码） | Cron 精度受限于轮询间隔，秒级任务会丢触发 |

---

## 模块说明

```
super-async/
├── super-async-sdk/          # 客户端 SDK + Worker 运行时
│   ├── SuperAsyncClient       # HTTP 客户端（提交任务、注册定时任务）
│   ├── SuperAsyncWorkerEngine # Worker 轮询与执行引擎
│   └── @SuperAsyncWorker      # Worker 处理器注解
├── super-async-server/       # 调度服务端
│   ├── TaskPollingScheduler   # 本地任务轮询调度器
│   ├── TaskExecutorEngine     # 本地任务执行引擎
│   ├── JobSchedulerEngine     # 定时任务调度引擎
│   ├── WorkflowEngine         # DAG 工作流引擎
│   └── REST API               # 任务管理 + Worker 通信接口
└── super-async-benchmark/    # 性能测试与接入示例
    ├── BenchmarkOrchestrator  # 多场景测试编排器
    ├── BenchmarkTaskController# 双模式任务处理器示例
    └── BenchmarkReport        # 性能指标与报告生成
```

---

## 开源协议

本项目基于 [Apache License 2.0](LICENSE) 开源。

---

## 参与贡献

欢迎 Issue 和 PR！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/xxx`)
3. 提交更改 (`git commit -am 'feat: xxx'`)
4. 推送分支 (`git push origin feature/xxx`)
5. 创建 Pull Request

---

> **注意**：SuperAsync 目前处于积极开发阶段，API 可能在正式 1.0 发布前发生变动。生产环境使用前建议进行充分压测与容错验证。
