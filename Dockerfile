FROM maven:3.8-openjdk-8-slim AS builder

# 复制源代码
WORKDIR /build
COPY pom.xml .
COPY src ./src

# 编译项目
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:8-jdk-alpine

# 工作目录
WORKDIR /app

# 从编译阶段复制JAR
COPY --from=builder /build/target/TiebaSignIn-Docker-1.0-SNAPSHOT.jar app.jar

# 创建数据与日志目录
RUN mkdir -p /app/data /app/logs

# 暴露端口
EXPOSE 11111

# 设置时区为亚洲/东京
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Tokyo" > /etc/timezone && \
    apk del tzdata

# 设置容器启动命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"]