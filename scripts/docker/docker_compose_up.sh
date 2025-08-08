#!/bin/bash

if [[ ( -z "${DATABASE_ADMIN_ROLE_PASSWORD}" ) || \
      ( -z "${DATABASE_ADMIN_KEY_PASSWORD}" ) || \
      ( -z "${DATABASE_SERVER_URL}" ) || \
      ( -z "${APP_ROLE_PASSWORD}" ) || \
      ( -z "${APP_KEY_PASSWORD}" ) || \
      ( -z "${BOT_TOKEN}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

sudo \
env \
DATABASE_ADMIN_ROLE_PASSWORD="${DATABASE_ADMIN_ROLE_PASSWORD}" \
DATABASE_ADMIN_KEY_PASSWORD="${DATABASE_ADMIN_KEY_PASSWORD}" \
DATABASE_SERVER_URL="${DATABASE_SERVER_URL}" \
APP_ROLE_PASSWORD="${APP_ROLE_PASSWORD}" \
APP_KEY_PASSWORD="${APP_KEY_PASSWORD}" \
BOT_TOKEN="${BOT_TOKEN}" \
bash << "EOF"

echo "Running as $(whoami)."
echo "Changing directory to project root."
cd ../..

docker compose up --build

echo "Finished running as $(whoami)."

EOF

echo