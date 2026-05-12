# SuperAsync — Claude Code 工作指南

## Maven 发布

- `super-async-sdk/pom.xml` 已配置 `distributionManagement`（id: `gitlab-maven`），发布到 `gitlab.local.caixy.xin` 私有仓库
- 发布命令：`cd super-async-sdk && mvn clean deploy`
- Release 版本不可重复发布，Snapshot 可反复覆盖

## 依赖拉取

- **项目 pom.xml 无需配置 `<repositories>`**，`~/.m2/settings.xml` 中的 `gitlab` profile 已全局生效
- 直接声明依赖即可使用

## 本地部署
执行前需要修改[Dockerfile](Dockerfile)对应的服务端版本号
```bash
./super-async/k8s/deploy-local.sh
```

## 环境要求

- Java 21
- Maven 使用自定义 truststore（`~/.m2/gitlab-truststore.jks`），已配置 mkcert 根证书
