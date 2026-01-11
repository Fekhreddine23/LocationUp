#!/usr/bin/env bash
set -euo pipefail

DATABASE_URL="${1:-${DATABASE_URL:-}}"
if [ -z "${DATABASE_URL}" ]; then
  echo "Usage: DATABASE_URL=... $0" >&2
  echo "   or: $0 <database_url>" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MIGRATION_PATH="${SCRIPT_DIR}/../src/main/resources/db/migration/V3__seed_demo_data.sql"

if command -v psql >/dev/null 2>&1; then
  psql "${DATABASE_URL}" -f "${MIGRATION_PATH}"
else
  docker run --rm -v "${MIGRATION_PATH}:/migrations/seed.sql" postgres:15 \
    psql "${DATABASE_URL}" -f /migrations/seed.sql
fi
