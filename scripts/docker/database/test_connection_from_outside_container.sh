#!/bin/bash

if [[ ( -z "${CA_CRT}" ) || \
      ( -z "${DATABASE_ADMIN_USER}" ) || \
      ( -z "${DATABASE_CERTS_DIRECTORY}" ) || \
      ( -z "${DATABASE_ADMIN_KEY}" ) || \
      ( -z "${DATABASE_ADMIN_KEY_PASSWORD}" ) || \
      ( -z "${DATABASE_ADMIN_CRT}" ) || \
      ( -z "${DATABASE_NAME}" ) || \
      ( -z "${APP_CERTS_DIRECTORY}" ) || \
      ( -z "${APP_KEY}" ) || \
      ( -z "${APP_KEY_PASSWORD}" ) || \
      ( -z "${APP_CRT}" ) || \
      ( -z "${APP_ROLE}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

run_or_exit()
{
    "$@"
    local RESULT=$?
    if [[ "${RESULT}" -ne 0 ]]; then
        exit 1
    fi
}

echo "Connecting as ${DATABASE_ADMIN_USER}"
run_or_exit sudo -u "${DATABASE_ADMIN_USER}" \
psql \
"postgresql://${DATABASE_ADMIN_USER}@localhost:5433/${DATABASE_NAME}?sslcertmode=require&sslmode=verify-full&sslkey=${DATABASE_CERTS_DIRECTORY}/${DATABASE_ADMIN_KEY}&sslpassword=${DATABASE_ADMIN_KEY_PASSWORD}&sslcert=${DATABASE_CERTS_DIRECTORY}/${DATABASE_ADMIN_CRT}&sslrootcert=${DATABASE_CERTS_DIRECTORY}/${CA_CRT}" \
-c "SELECT audio_class_id FROM audio_class;"

echo "Connecting as ${APP_ROLE}"
run_or_exit psql \
"postgresql://${APP_ROLE}@localhost:5433/${DATABASE_NAME}?sslcertmode=require&sslmode=verify-full&sslkey=${APP_CERTS_DIRECTORY}/${APP_KEY}&sslpassword=${APP_KEY_PASSWORD}&sslcert=${APP_CERTS_DIRECTORY}/${APP_CRT}&sslrootcert=${APP_CERTS_DIRECTORY}/${CA_CRT}" \
-c "SELECT audio_class_id FROM audio_class;"

echo