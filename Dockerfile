# SuperAsync Server Runtime
FROM arm64v8/maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY super-async/super-async-server/target/super-async-server-1.1.2-exec.jar app.jar
RUN apt-get update && apt-get install -y tzdata && rm -rf /var/lib/apt/lists/*
ENV TZ=Asia/Shanghai
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
