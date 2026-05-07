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
2. **本地模式**：`TaskPollingScheduler` 定期轮询 `PENDING` 任务（默认 5s，可配置），调用 `TaskExecutorEngine` 在线程池中执行。任务提交后会**即时触发一次轮询**，消除等待延迟。
3. **Worker 模式**：`SuperAsyncWorkerEngine` 定期 HTTP 轮询 `/v1/worker/poll`，抢到任务后在本机线程池执行，结果通过 `/v1/worker/complete` 回写。支持**批量抢锁**（一次 HTTP 拉取多条任务）。
4. **定时任务**：`JobSchedulerEngine` 定期扫描 `scheduled_jobs` 表（默认 10s，可配置），触达时间的任务会生成对应的 `async_task`。

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

### 测试结果（优化后）

> 优化内容：可配置轮询间隔 + 提交后即时调度（eager poll）+ Worker 批量抢锁

#### 简单任务延迟测试 (Worker模式-手动驱动)

| 指标 | 优化前 | 优化后 | 提升 |
|---|---|---|---|
| 平均延迟 | 415.34 ms | **253.00 ms** | **↓ 39%** |
| P50 延迟 | 423 ms | **245 ms** | **↓ 42%** |
| P95 延迟 | 735 ms | **434 ms** | **↓ 41%** |
| P99 延迟 | 786 ms | **489 ms** | **↓ 38%** |

#### 简单任务吞吐量测试 (本地模式-手动驱动)

| 指标 | 优化前 | 优化后 | 提升 |
|---|---|---|---|
| 平均延迟 | 2100.84 ms | **34.30 ms** | **↓ 98%** |
| P50 延迟 | 1911 ms | **33 ms** | **↓ 98%** |
| P95 延迟 | 3893 ms | **54 ms** | **↓ 99%** |
| P99 延迟 | 4095 ms | **76 ms** | **↓ 98%** |
| 吞吐量 | 3846 tasks/s | **3937 tasks/s** | 持平 |

#### 耗时任务压力测试 (本地模式-手动驱动)

| 指标 | 优化前 | 优化后 |
|---|---|---|
| 平均延迟 | 2663.82 ms | **2650.30 ms** |
| P50 延迟 | 3014 ms | **3002 ms** |

### 结果分析

- **本地模式延迟**：通过**即时调度（eager poll）**，任务提交后毫秒级触发 `pollAndDispatch`，P50 从 **1911ms 降至 33ms**，达到百毫秒级调度能力。
- **Worker 模式延迟**：通过**批量抢锁**（一次 HTTP 拉取 10 条任务），HTTP 往返次数减少 90%，P50 从 **423ms 降至 245ms**。
- **耗时任务**：优化前后基本持平，因为瓶颈在任务执行耗时（1000ms 休眠），而非调度层。

### 调优建议

| 配置项 | 默认值 | 生产建议 | 说明 |
|---|---|---|---|
| `superasync.scheduler.poll-interval-ms` | 5000 | 200 ~ 1000 | 本地调度轮询间隔。eager poll 已消除大部分延迟，此项作为兜底 |
| `superasync.job-scheduler.poll-interval-ms` | 10000 | 1000 ~ 5000 | 定时任务扫描间隔。Cron 精度受限于该值 |
| `superasync.worker.poll-interval-ms` | 3000 | 1000 ~ 3000 | Worker 轮询间隔。过低会增加 DB 抢锁 QPS |
| `superasync.worker.core-pool-size` | 16 | 按 CPU 核数 × 2 | Worker 执行线程池大小 |
| `superasync.executor.core-pool-size` | 16 | 按 CPU 核数 × 2 | Server 本地执行线程池大小 |

---

## 配置项参考

`application.yml` 中所有可配置项：

```yaml
superasync:
  node: server-node-1                    # 当前节点标识，用于任务锁定归属
  scheduler:
    poll-interval-ms: 5000               # 本地任务轮询间隔（默认 5s）
    batch-size: 200                      # 单次轮询最大任务数（建议 200~500）
  executor:
    core-pool-size: 16                   # 本地执行线程池核心数
    max-pool-size: 32                    # 本地执行线程池最大数
  retry:
    base-delay-seconds: 5                # 失败重试基础退避时间
    max-delay-minutes: 10                # 失败重试最大退避时间
  timeout:
    check-interval-ms: 30000             # 超时任务扫描间隔
    default-timeout-minutes: 10          # 任务默认超时时间
  job-scheduler:
    poll-interval-ms: 10000              # 定时任务扫描间隔（默认 10s）
  worker:
    enabled: true                        # 是否启用 Worker 模式
    worker-id: worker-1                  # Worker 唯一标识
    server-url: http://localhost:8080    # Server 地址
    core-pool-size: 16                   # Worker 执行线程池大小
    poll-interval-ms: 3000               # Worker 轮询间隔（默认 3s）
    tags:                                # Worker 标签，用于任务路由
      - PAYMENT_WORKER
```

### 关键行为说明

- **eager poll（即时调度）**：任务 `submit()` 落库后，Server 会立即异步触发一次 `pollAndDispatch`。即使轮询间隔配置为 5s，本地任务也能在**毫秒级**被调度，大幅降低延迟 floor。
- **Worker 批量抢锁**：新版 SDK 默认一次拉取 10 条任务到本地队列再分发执行。老版 SDK 走单条抢锁，仍然兼容，只是 HTTP 效率略低。
- **配置热生效**：`poll-interval-ms` 等 Spring `@Scheduled` 配置在应用重启后生效，不支持运行时动态刷新。

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
