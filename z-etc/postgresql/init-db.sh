#!/bin/bash

set -e

echo "🔧 Running init-db.sh..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE SCHEMA IF NOT EXISTS core;
    CREATE SCHEMA IF NOT EXISTS identity;
    CREATE SCHEMA IF NOT EXISTS metric;
    CREATE EXTENSION IF NOT EXISTS postgis;
    CREATE EXTENSION IF NOT EXISTS timescaledb;
EOSQL

echo "✅ init-db.sh finished."
