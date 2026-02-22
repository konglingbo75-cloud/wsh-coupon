# ============================================================
# 微生活券吧 - 后端服务 Dockerfile
# 多阶段构建：Maven 构建 + JRE 运行
# ============================================================

# 阶段1：Maven 构建
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制 pom 文件，利用 Docker 层缓存
COPY pom.xml .
COPY wsh-common/pom.xml wsh-common/
COPY wsh-common/common-core/pom.xml wsh-common/common-core/
COPY wsh-common/common-redis/pom.xml wsh-common/common-redis/
COPY wsh-common/common-security/pom.xml wsh-common/common-security/
COPY wsh-common/common-mybatis/pom.xml wsh-common/common-mybatis/
COPY wsh-service/pom.xml wsh-service/

# 下载依赖（利用缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY wsh-common wsh-common
COPY wsh-service wsh-service

# 构建（跳过测试加速构建）
RUN mvn clean package -DskipTests -B

# 阶段2：运行时镜像
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="WSH Team"
LABEL description="微生活券吧后端服务"

# 设置时区
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata curl && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 创建非 root 用户
RUN addgroup -S wsh && adduser -S wsh -G wsh

WORKDIR /app

# 从构建阶段复制 JAR
COPY --from=builder /build/wsh-service/target/wsh-service-*.jar app.jar

# 切换到非 root 用户
USER wsh

# 暴露端口
EXPOSE 9000

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9000/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
