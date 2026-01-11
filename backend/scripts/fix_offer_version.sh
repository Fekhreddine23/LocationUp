#!/usr/bin/env bash
set -euo pipefail

DATABASE_URL="${1:-${DATABASE_URL:-}}"
if [ -z "${DATABASE_URL}" ]; then
  echo "Usage: DATABASE_URL=... $0" >&2
  echo "   or: $0 <database_url>" >&2
  exit 1
fi

SQL="ALTER TABLE offers ADD COLUMN IF NOT EXISTS version BIGINT;
ALTER TABLE offers ALTER COLUMN version SET DEFAULT 0;
UPDATE offers SET version = 0 WHERE version IS NULL;
ALTER TABLE offers ALTER COLUMN version SET NOT NULL;"

if command -v psql >/dev/null 2>&1; then
  psql "${DATABASE_URL}" -c "${SQL}"
else
  docker run --rm postgres:15 psql "${DATABASE_URL}" -c "${SQL}"
fi
