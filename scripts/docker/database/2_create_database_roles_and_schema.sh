#!/bin/bash

if [[ ( -z "${CA_CRT}" ) || \
      ( -z "${POSTGRESQL_MAJOR_VERSION}" ) || \
      ( -z "${DATABASE_ADMIN_USER}" ) || \
      ( -z "${DATABASE_ADMIN_KEY}" ) || \
      ( -z "${DATABASE_ADMIN_KEY_PASSWORD}" ) || \
      ( -z "${DATABASE_ADMIN_CRT}" ) || \
      ( -z "${DATABASE_NAME}" ) || \
      ( -z "${DATABASE_SERVER_HOSTNAME}" ) || \
      ( -z "${DATABASE_SERVER_PORT}" ) || \
      ( -z "${APP_ROLE}" ) || \
      ( -z "${APP_ROLE_PASSWORD}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

sudo -u "${DATABASE_ADMIN_USER}" \
env \
CA_CRT="${CA_CRT}" \
POSTGRESQL_MAJOR_VERSION="${POSTGRESQL_MAJOR_VERSION}" \
DATABASE_ADMIN_USER="${DATABASE_ADMIN_USER}" \
DATABASE_ADMIN_KEY="${DATABASE_ADMIN_KEY}" \
DATABASE_ADMIN_KEY_PASSWORD="${DATABASE_ADMIN_KEY_PASSWORD}" \
DATABASE_ADMIN_CRT="${DATABASE_ADMIN_CRT}" \
DATABASE_NAME="${DATABASE_NAME}" \
DATABASE_SERVER_HOSTNAME="${DATABASE_SERVER_HOSTNAME}" \
DATABASE_SERVER_PORT="${DATABASE_SERVER_PORT}" \
APP_ROLE="${APP_ROLE}" \
APP_ROLE_PASSWORD="${APP_ROLE_PASSWORD}" \
bash << "EOF"

DATABASE_ADMIN_HOME=$(getent passwd "${DATABASE_ADMIN_USER}" | cut -d : -f 6)
DATABASE_DATA_DIRECTORY=\
"${DATABASE_ADMIN_HOME}/${POSTGRESQL_MAJOR_VERSION}/main"

echo "Running as $(whoami)."

psql \
"postgresql://${DATABASE_ADMIN_USER}@${DATABASE_SERVER_HOSTNAME}:${DATABASE_SERVER_PORT}/postgres?sslcertmode=require&sslmode=verify-full&sslkey=${DATABASE_DATA_DIRECTORY}/${DATABASE_ADMIN_KEY}&sslpassword=${DATABASE_ADMIN_KEY_PASSWORD}&sslcert=${DATABASE_DATA_DIRECTORY}/${DATABASE_ADMIN_CRT}&sslrootcert=${DATABASE_DATA_DIRECTORY}/${CA_CRT}" \
-f create_database_roles_and_schema.sql \
-v app_role="${APP_ROLE}" \
-v app_role_password="${APP_ROLE_PASSWORD}" \
-v database_name="${DATABASE_NAME}"

echo "Finished running as $(whoami)."

EOF

echo