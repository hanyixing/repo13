# syntax=docker/dockerfile:1
# ------------------------------------------------------------------
# blade-examples container image (multi-stage)
#
# IMPORTANT: build from the REPOSITORY ROOT so the reactor modules
# (blade-core / blade-kit / blade-security) are part of the context:
#
#   docker build -f blade-examples/Dockerfile -t blade-examples .
#
# Override the runtime profile with -e BLADE_ENV=dev|test|prod.
# ------------------------------------------------------------------

# ---- Stage 1: build the runnable fat jar ----
FROM maven:3-eclipse-temurin-8 AS build
WORKDIR /workspace

# Copy the whole reactor and build only blade-examples + its upstream modules.
COPY . .
RUN mvn -B -q -pl blade-examples -am -DskipTests clean package

# ---- Stage 2: slim runtime ----
FROM eclipse-temurin:8-jre-alpine
WORKDIR /app

# Default profile; loads classpath application-${BLADE_ENV}.properties at startup.
ENV BLADE_ENV=prod

COPY --from=build /workspace/blade-examples/target/blade-examples.jar /app/app.jar

EXPOSE 9001

# /hello is a no-auth GET that returns JSON - a cheap liveness probe.
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
    CMD wget -q --spider http://localhost:9001/hello || exit 1

# exec form + exec so the JVM is PID 1 and receives SIGTERM for clean shutdown.
ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar --app.env=${BLADE_ENV}"]
