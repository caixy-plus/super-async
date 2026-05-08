# SuperAsync Server Runtime
FROM arm64v8/maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY super-async/super-async-server/target/super-async-server-1.0.0-exec.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
