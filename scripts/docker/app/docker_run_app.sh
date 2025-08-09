#!/bin/bash

if [[ ( -z "${APP_NAME}" ) || \
      ( -z "${ARTIFACT_ID}" ) || \
      ( -z "${BOT_TOKEN}" ) || \
      ( -z "${VOICE_EXTENSION}" ) || \
      ( -z "${TIME_ZONE}" ) || \
      ( -z "${HOUR_TO_REMIND}" ) || \
      ( -z "${DATABASE_SERVER_URL}" ) || \
      ( -z "${APP_ROLE}" ) || \
      ( -z "${APP_ROLE_PASSWORD}" ) || \
      ( -z "${APP_CERTS_DIRECTORY}" ) || \
      ( -z "${APP_AUDIO_DIRECTORY}" ) || \
      ( -z "${APP_DER_KEY}" ) || \
      ( -z "${APP_KEY_PASSWORD}" ) || \
      ( -z "${APP_CRT}" ) || \
      ( -z "${CA_CRT}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

APP_USER_UID=$(id -u)
APP_USER_GID=$(id -g)

run_or_exit()
{
    "$@"
    local RESULT=$?
    if [[ "${RESULT}" -ne 0 ]]; then
        exit 1
    fi
}

run_or_exit sudo docker build \
-t "${APP_NAME}" \
-f ./Dockerfile \
--build-arg ARTIFACT_ID="${ARTIFACT_ID}" \
--build-arg APP_CERTS_DIRECTORY="${APP_CERTS_DIRECTORY}" \
--build-arg APP_AUDIO_DIRECTORY="${APP_AUDIO_DIRECTORY}" \
--progress=plain \
../../../

run_or_exit sudo docker run \
--name "container_${APP_NAME}" \
--user ${APP_USER_UID}:${APP_USER_GID} \
-e ARTIFACT_ID="${ARTIFACT_ID}" \
-e BOT_TOKEN="${BOT_TOKEN}" \
-e VOICE_EXTENSION="${VOICE_EXTENSION}" \
-e TIME_ZONE="${TIME_ZONE}" \
-e HOUR_TO_REMIND="${HOUR_TO_REMIND}" \
-e DATABASE_SERVER_URL="${DATABASE_SERVER_URL}" \
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

echo