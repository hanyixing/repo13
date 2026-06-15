#!/usr/bin/env bash
#
# Stop the blade-examples app started by ./start.sh.
# Sends SIGTERM, waits, then escalates to SIGKILL if still alive.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

PID_FILE="blade.pid"
TIMEOUT="${STOP_TIMEOUT:-15}"

if [[ ! -f "$PID_FILE" ]]; then
    echo "No $PID_FILE found; nothing to stop."
    exit 0
fi

PID="$(cat "$PID_FILE")"

if ! kill -0 "$PID" 2>/dev/null; then
    echo "Process $PID not running; removing stale $PID_FILE."
    rm -f "$PID_FILE"
    exit 0
fi

echo "Stopping blade-examples (pid $PID) ..."
kill "$PID" 2>/dev/null || true

for _ in $(seq 1 "$TIMEOUT"); do
    if ! kill -0 "$PID" 2>/dev/null; then
        break
    fi
    sleep 1
done

if kill -0 "$PID" 2>/dev/null; then
    echo "Did not stop in ${TIMEOUT}s; sending SIGKILL."
    kill -9 "$PID" 2>/dev/null || true
fi

rm -f "$PID_FILE"
echo "Stopped."
