#!/bin/bash

if [[ "$(id -u)" -ne 0 ]]; then
    echo "Not running as root. Re-running as root."
    exec sudo -E "$0" "$@"
fi

run_or_exit()
{
    "$@"
    local RESULT=$?
    if [[ "${RESULT}" -ne 0 ]]; then
        exit 1
    fi
}

echo "Running as $(whoami)."

run_or_exit docker build \
-t "${APP_NAME}" \
-f ../../../app.Dockerfile \
--build-arg ARTIFACT_ID="${ARTIFACT_ID}" \
--build-arg APP_CERTS_DIRECTORY="${APP_CERTS_DIRECTORY}" \
--build-arg APP_AUDIO_DIRECTORY="${APP_AUDIO_DIRECTORY}" \
--progress=plain \
../../../

run_or_exit docker run \
--name "container_${APP_NAME}" \
--user ${APP_USER_UID}:${APP_USER_GID} \
-e ARTIFACT_ID="${ARTIFACT_ID}" \
-e BOT_TOKEN="${BOT_TOKEN}" \
-e VOICE_EXTENSION="${VOICE_EXTENSION}" \
-e TIME_ZONE="${TIME_ZONE}" \
-e HOUR_TO_REMIND="${HOUR_TO_REMIND}" \
-e APP_LOGS_DIRECTORY="${APP_LOGS_DIRECTORY}" \
-e DATABASE_SERVER_HOSTNAME="${DATABASE_SERVER_HOSTNAME}" \
-e DATABASE_NAME="${DATABASE_NAME}" \
-e DATABASE_SERVER_PORT="${DATABASE_SERVER_PORT}" \
-e APP_ROLE="${APP_ROLE}" \
-e APP_ROLE_PASSWORD="${APP_ROLE_PASSWORD}" \
-e APP_CERTS_DIRECTORY="${APP_CERTS_DIRECTORY}" \
-e APP_AUDIO_DIRECTORY="${APP_AUDIO_DIRECTORY}" \
-e APP_DER_KEY="${APP_DER_KEY}" \
-e APP_KEY_PASSWORD="${APP_KEY_PASSWORD}" \
-e APP_CRT="${APP_CRT}" \
-e CA_CRT="${CA_CRT}" \
--mount "type=bind,src=${APP_CERTS_DIRECTORY},dst=${APP_CERTS_DIRECTORY}" \
--mount "type=bind,src=${APP_AUDIO_DIRECTORY},dst=${APP_AUDIO_DIRECTORY}" \
-i \
-t \
--rm \
"${APP_NAME}"

echo "Finished running as $(whoami)."

echo