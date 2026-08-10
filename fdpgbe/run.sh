#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if [[ -f .env ]]; then
  set -a
  source .env
  set +a
else
  echo "Missing .env file. Create one from .env.example first." >&2
  exit 1
fi

export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://${DB_HOST:-localhost}:${DB_PORT:-5432}/${DB_NAME:-fdpg}}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-${DB_USERNAME:-postgres}}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-${DB_PASSWORD:-password}}"

export SERVER_PORT="${SERVER_PORT:-8080}"
export SPRING_JPA_HIBERNATE_DDL_AUTO="${SPRING_JPA_HIBERNATE_DDL_AUTO:-update}"
export SPRING_JPA_SHOW_SQL="${SPRING_JPA_SHOW_SQL:-true}"
export SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL="${SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL:-true}"
export JWT_SECRET="${JWT_SECRET:-averylongsecretkeythatisatleast256bitslongforhs256!}"
export JWT_EXPIRATION="${JWT_EXPIRATION:-86400000}"

exec ./mvnw spring-boot:run
