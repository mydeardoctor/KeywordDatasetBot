#!/bin/bash

cd ../common
set -a && source .env && set +a

cd ../docker
set -a && source .env && set +a

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

docker compose down

echo "Finished running as $(whoami)."

EOF

echo