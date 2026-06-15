#!/usr/bin/env bash
#
# Deploy the blade-examples service.
#
#   ./deploy.sh                 # MODE=docker (default): build image + (re)start via compose
#   MODE=jar ./deploy.sh        # build jar + restart the local background process
#   GIT_PULL=true ./deploy.sh   # git pull latest before building
#
# Env vars: MODE=docker|jar, BLADE_ENV=dev|test|prod, GIT_PULL=true|false
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

MODE="${MODE:-docker}"
BLADE_ENV="${BLADE_ENV:-prod}"
GIT_PULL="${GIT_PULL:-false}"

if [[ "$GIT_PULL" == "true" ]]; then
    echo "==> Pulling latest changes"
    git pull --ff-only
fi

case "$MODE" in
    docker)
        echo "==> Deploying with docker compose (BLADE_ENV=$BLADE_ENV)"
        BLADE_ENV="$BLADE_ENV" docker compose up -d --build
        docker compose ps
        ;;
    jar)
        echo "==> Building jar"
        mvn -B -pl blade-examples -am -DskipTests clean package
        echo "==> Restarting local process"
        ./stop.sh || true
        BLADE_ENV="$BLADE_ENV" ./start.sh
        ;;
    *)
        echo "Unknown MODE='$MODE' (expected 'docker' or 'jar')." >&2
        exit 2
        ;;
esac

echo "==> Deploy complete."
