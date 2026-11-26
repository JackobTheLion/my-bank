#!/bin/sh

set -eu

psql --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
    --set=ON_ERROR_STOP=1 \
    --set=accounts_schema="$ACCOUNTS_DATABASE_SCHEMA" \
    --set=cash_schema="$CASH_DATABASE_SCHEMA" \
    --set=front_ui_schema="$FRONT_UI_DATABASE_SCHEMA" \
    --set=gateway_schema="$GATEWAY_DATABASE_SCHEMA" \
    --set=keycloak_schema="$KEYCLOAK_DB_SCHEMA" \
    --set=notifications_schema="$NOTIFICATIONS_DATABASE_SCHEMA" \
    --set=transfer_schema="$TRANSFER_DATABASE_SCHEMA" <<'SQL'
CREATE SCHEMA IF NOT EXISTS :"accounts_schema";
CREATE SCHEMA IF NOT EXISTS :"cash_schema";
CREATE SCHEMA IF NOT EXISTS :"front_ui_schema";
CREATE SCHEMA IF NOT EXISTS :"gateway_schema";
CREATE SCHEMA IF NOT EXISTS :"keycloak_schema";
CREATE SCHEMA IF NOT EXISTS :"notifications_schema";
CREATE SCHEMA IF NOT EXISTS :"transfer_schema";
SQL
