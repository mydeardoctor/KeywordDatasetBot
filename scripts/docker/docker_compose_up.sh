#!/bin/bash

if [[ ( -z "${DATABASE_ADMIN_ROLE_PASSWORD}" ) || \
      ( -z "${DATABASE_ADMIN_KEY_PASSWORD}" ) || \
      ( -z "${DATABASE_SERVER_HOSTNAME}" ) || \
      ( -z "${DATABASE_NAME}" ) || \
      ( -z "${DATABASE_SERVER_PORT}" ) || \
      ( -z "${APP_ROLE_PASSWORD}" ) || \
      ( -z "${APP_KEY_PASSWORD}" ) || \
      ( -z "${BOT_TOKEN}" ) || \
      ( -z "${TIME_ZONE}" ) || \
      ( -z "${HOUR_TO_REMIND}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

sudo \
env \
DATABASE_ADMIN_ROLE_PASSWORD="${DATABASE_ADMIN_ROLE_PASSWORD}" \
DATABASE_ADMIN_KEY_PASSWORD="${DATABASE_ADMIN_KEY_PASSWORD}" \
DATABASE_SERVER_HOSTNAME="${DATABASE_SERVER_HOSTNAME}" \
DATABASE_NAME="${DATABASE_NAME}" \
DATABASE_SERVER_PORT="${DATABASE_SERVER_PORT}" \
APP_ROLE_PASSWORD="${APP_ROLE_PASSWORD}" \
APP_KEY_PASSWORD="${APP_KEY_PASSWORD}" \
BOT_TOKEN="${BOT_TOKEN}" \
TIME_ZONE="${TIME_ZONE}" \
HOUR_TO_REMIND="${HOUR_TO_REMIND}" \
bash << "EOF"

echo "Running as $(whoami)."
echo "Changing directory to project root."
cd ../..

docker compose up --build

echo "Finished running as $(whoami)."

EOF

echo