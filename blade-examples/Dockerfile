# Build stage
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /build

# Copy parent pom first for better layer caching
COPY pom.xml .
COPY blade-core/pom.xml blade-core/
COPY blade-kit/pom.xml blade-kit/
COPY blade-security/pom.xml blade-security/
COPY blade-websocket/pom.xml blade-websocket/
COPY blade-examples/pom.xml blade-examples/

# Download dependencies (cached unless pom files change)
RUN mvn dependency:go-offline -pl blade-examples -am -B 2>/dev/null || true

# Copy source code
COPY blade-core/src blade-core/src
COPY blade-kit/src blade-kit/src
COPY blade-security/src blade-security/src
COPY blade-websocket/src blade-websocket/src
COPY blade-examples/src blade-examples/src

# Build the fat jar
RUN mvn clean package -pl blade-examples -am -DskipTests -B

# Runtime stage
FROM openjdk:8-jre-slim

LABEL maintainer="hellokaton <hellokaton@gmail.com>"
LABEL description="Blade Framework Examples Application"

RUN groupadd -r blade && useradd -r -g blade blade

WORKDIR /app

COPY --from=builder /build/blade-examples/target/blade-examples-*.jar app.jar

# Copy environment-specific configs (override at runtime via volume mounts)
COPY blade-examples/src/main/resources/application*.properties /app/config/

RUN chown -R blade:blade /app

USER blade

EXPOSE 9005

# JVM options with sensible defaults for containers
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"
ENV BLADE_ENV=prod

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -sf http://localhost:9005/hello || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dblade.env=$BLADE_ENV -jar app.jar"]
