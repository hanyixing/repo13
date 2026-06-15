#!/bin/bash
#
# deploy.sh - Build and deploy the Blade examples application
#
# Usage:
#   ./deploy.sh              # Deploy locally with Docker Compose
#   ./deploy.sh --local      # Deploy locally without Docker
#   ./deploy.sh --release    # Build and publish to Maven Central
#   ./deploy.sh --docker     # Build Docker image only
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="blade-examples"
BLADE_ENV="${BLADE_ENV:-prod}"
DEPLOY_MODE="${1:---docker}"

# Ensure JAVA_HOME is set
export JAVA_HOME="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which java))))}"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

build_project() {
    log "Building project..."
    cd "${SCRIPT_DIR}"
    mvn clean install -DskipTests -B
    log "Build completed successfully"
}

deploy_local() {
    log "Deploying locally (env: ${BLADE_ENV})..."
    bash "${SCRIPT_DIR}/stop.sh"
    bash "${SCRIPT_DIR}/start.sh"
    log "Local deployment completed"
}

deploy_docker() {
    log "Deploying with Docker Compose (env: ${BLADE_ENV})..."
    cd "${SCRIPT_DIR}"

    # Build Docker image
    docker-compose build

    # Stop existing containers
    docker-compose down

    # Start services
    docker-compose up -d

    # Wait for health check
    log "Waiting for application to start..."
    local retries=0
    while [ ${retries} -lt 30 ]; do
        if curl -sf http://localhost:9005/hello >/dev/null 2>&1; then
            log "Application is healthy and running"
            return 0
        fi
        sleep 2
        retries=$((retries + 1))
    done

    log "ERROR: Application failed to start within 60 seconds"
    docker-compose logs
    exit 1
}

deploy_release() {
    log "Preparing Maven Central release..."
    cd "${SCRIPT_DIR}"

    # Verify GPG is available
    if ! command -v gpg &>/dev/null; then
        log "ERROR: GPG is required for release deployment"
        exit 1
    fi

    # Run tests first
    log "Running tests..."
    mvn clean test -B

    # Deploy to Maven Central via release profile
    log "Deploying to Maven Central (requires GPG passphrase and OSSRH credentials)..."
    mvn clean deploy -Prelease -B

    log "Release deployment completed. Check https://s01.oss.sonatype.org/ for staging repository."
}

deploy_benchmark() {
    log "Running JMH benchmarks..."
    cd "${SCRIPT_DIR}/blade-benchmark"
    mvn clean package -DskipTests -B
    java -jar target/benchmarks.jar -wi 3 -i 5 -f 1 -tu s -rf json -rff target/jmh-result.json
    log "Benchmark results saved to blade-benchmark/target/jmh-result.json"
}

case "${DEPLOY_MODE}" in
    --local)
        build_project
        deploy_local
        ;;
    --release)
        build_project
        deploy_release
        ;;
    --docker)
        deploy_docker
        ;;
    --benchmark)
        build_project
        deploy_benchmark
        ;;
    --help|-h)
        echo "Usage: $0 [OPTIONS]"
        echo ""
        echo "Options:"
        echo "  --local      Build and deploy locally without Docker"
        echo "  --docker     Build and deploy with Docker Compose (default)"
        echo "  --release    Build and publish to Maven Central"
        echo "  --benchmark  Build and run JMH performance benchmarks"
        echo "  --help       Show this help message"
        echo ""
        echo "Environment variables:"
        echo "  BLADE_ENV    Application environment: dev, test, prod (default: prod)"
        echo "  JAVA_OPTS    JVM options (default: -Xms256m -Xmx512m -XX:+UseG1GC)"
        echo "  JAVA_HOME    Java installation directory"
        ;;
    *)
        echo "Unknown option: ${DEPLOY_MODE}"
        echo "Run '$0 --help' for usage information"
        exit 1
        ;;
esac
