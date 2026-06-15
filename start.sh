#!/bin/bash
#
# start.sh - Start the Blade examples application
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="blade-examples"
JAR_FILE="${SCRIPT_DIR}/blade-examples/target/blade-examples-2.1.2.RELEASE.jar"
PID_FILE="${SCRIPT_DIR}/${APP_NAME}.pid"
LOG_DIR="${SCRIPT_DIR}/logs"
BLADE_ENV="${BLADE_ENV:-prod}"
JAVA_OPTS="${JAVA_OPTS:--Xms256m -Xmx512m -XX:+UseG1GC}"

mkdir -p "${LOG_DIR}"

if [ -f "${PID_FILE}" ]; then
    PID=$(cat "${PID_FILE}")
    if kill -0 "${PID}" 2>/dev/null; then
        echo "[WARN] ${APP_NAME} is already running (PID: ${PID})"
        exit 1
    else
        rm -f "${PID_FILE}"
    fi
fi

if [ ! -f "${JAR_FILE}" ]; then
    echo "[INFO] JAR file not found, building project..."
    cd "${SCRIPT_DIR}"
    export JAVA_HOME="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which java))))}"
    mvn clean package -pl blade-examples -am -DskipTests -B
fi

echo "[INFO] Starting ${APP_NAME} (env: ${BLADE_ENV})..."

nohup java ${JAVA_OPTS} \
    -Dblade.env="${BLADE_ENV}" \
    -jar "${JAR_FILE}" \
    >> "${LOG_DIR}/${APP_NAME}.log" 2>&1 &

PID=$!
echo "${PID}" > "${PID_FILE}"

sleep 3
if kill -0 "${PID}" 2>/dev/null; then
    echo "[OK] ${APP_NAME} started successfully (PID: ${PID})"
    echo "[INFO] Log file: ${LOG_DIR}/${APP_NAME}.log"
else
    echo "[ERROR] ${APP_NAME} failed to start. Check log: ${LOG_DIR}/${APP_NAME}.log"
    rm -f "${PID_FILE}"
    exit 1
fi
