#!/bin/bash

if [[ ( -z "${DATABASE_ADMIN_USER}" ) || \
      ( -z "${DATABASE_NAME}" ) || \
      ( -z "${APP_ROLE}" ) || \
      ( -z "${APP_ROLE_PASSWORD}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

psql \
-U ${DATABASE_ADMIN_USER} \
-d postgres \
-f /home/create_database_roles_and_schema.sql \
-v app_role="${APP_ROLE}" \
-v app_role_password="${APP_ROLE_PASSWORD}" \
-v database_name="${DATABASE_NAME}"

echo