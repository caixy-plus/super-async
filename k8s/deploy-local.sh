#!/bin/bash
set -e

# SuperAsync 本地部署: 编译 → 构建镜像 → 重启 K8s
# 用法: 从 app-platform-all/ 运行
#   ./super-async/k8s/deploy-local.sh

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

echo "==> [1/3] Maven 编译 super-async"
cd "$ROOT/super-async"
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "$JAVA_HOME")
mvn package -DskipTests -q

echo "==> [2/3] 构建 Docker 镜像"
cd "$ROOT"
docker build -f super-async/Dockerfile -t app-platform/super-async:latest .

echo "==> [3/3] 重启 K8s"
kubectl rollout restart deployment super-async -n app-platform
kubectl rollout status deployment super-async -n app-platform --timeout=60s

echo "==> SuperAsync 部署完成 ✓"
