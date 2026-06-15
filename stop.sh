#!/bin/bash
#
# stop.sh - Stop the Blade examples application
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="blade-examples"
PID_FILE="${SCRIPT_DIR}/${APP_NAME}.pid"
GRACEFUL_TIMEOUT=30

stop_process() {
    local pid=$1
    echo "[INFO] Sending SIGTERM to ${APP_NAME} (PID: ${pid})..."
    kill "${pid}" 2>/dev/null || true

    local count=0
    while kill -0 "${pid}" 2>/dev/null && [ ${count} -lt ${GRACEFUL_TIMEOUT} ]; do
        sleep 1
        count=$((count + 1))
    done

    if kill -0 "${pid}" 2>/dev/null; then
        echo "[WARN] Process did not stop within ${GRACEFUL_TIMEOUT}s, sending SIGKILL..."
        kill -9 "${pid}" 2>/dev/null || true
        sleep 2
    fi
}

# Stop via PID file
if [ -f "${PID_FILE}" ]; then
    PID=$(cat "${PID_FILE}")
    if kill -0 "${PID}" 2>/dev/null; then
        stop_process "${PID}"
        echo "[OK] ${APP_NAME} stopped (PID: ${PID})"
    else
        echo "[WARN] ${APP_NAME} (PID: ${PID}) is not running"
    fi
    rm -f "${PID_FILE}"
else
    echo "[INFO] No PID file found, searching for process..."
    PIDS=$(pgrep -f "blade-examples.*\.jar" 2>/dev/null || true)
    if [ -n "${PIDS}" ]; then
        for pid in ${PIDS}; do
            stop_process "${pid}"
            echo "[OK] Stopped process ${pid}"
        done
    else
        echo "[INFO] ${APP_NAME} is not running"
    fi
fi

# Also stop Docker containers if running
if command -v docker-compose &>/dev/null; then
    cd "${SCRIPT_DIR}"
    if docker-compose ps --format json 2>/dev/null | grep -q "running"; then
        echo "[INFO] Stopping Docker containers..."
        docker-compose down
        echo "[OK] Docker containers stopped"
    fi
fi
