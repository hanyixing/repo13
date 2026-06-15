#!/usr/bin/env bash
#
# Start the blade-examples app as a background process.
#
#   ./start.sh                 # uses BLADE_ENV=dev
#   BLADE_ENV=prod ./start.sh  # production profile
#
# Builds the runnable jar first if it is missing (requires JAVA_HOME -> a JDK).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

BLADE_ENV="${BLADE_ENV:-dev}"
JAR="blade-examples/target/blade-examples.jar"
PID_FILE="blade.pid"
LOG_DIR="logs"
LOG_FILE="$LOG_DIR/app.log"

# Already running?
if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    echo "blade-examples already running (pid $(cat "$PID_FILE")). Run ./stop.sh first."
    exit 1
fi

# Build if needed.
if [[ ! -f "$JAR" ]]; then
    echo "Jar not found, building $JAR ..."
    mvn -B -pl blade-examples -am -DskipTests clean package
fi

mkdir -p "$LOG_DIR"

echo "Starting blade-examples (app.env=$BLADE_ENV) ..."
nohup java -jar "$JAR" --app.env="$BLADE_ENV" > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"

echo "Started (pid $(cat "$PID_FILE")). Logs: $LOG_FILE"
echo "Try: curl http://localhost:9001/hello"
