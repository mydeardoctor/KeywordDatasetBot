#!/bin/bash

if [[ ( -z "${APP_HOME}" ) || \
      ( -z "${APP_CERTS_DIRECTORY}" ) || \
      ( -z "${APP_AUDIO_DIRECTORY}" ) || \
      ( -z "${APP_LOGS_DIRECTORY}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

CURRENT_USER=$(id -un)
CURRENT_GROUP=$(id -gn)
APP_HOME_PERMISSIONS="700"
APP_CERTS_DIRECTORY_PERMISSIONS="700"
APP_AUDIO_DIRECTORY_PERMISSIONS="700"
APP_LOGS_DIRECTORY_PERMISSIONS="755"

sudo \
env \
CURRENT_USER="${CURRENT_USER}" \
CURRENT_GROUP="${CURRENT_GROUP}" \
APP_HOME="${APP_HOME}" \
APP_HOME_PERMISSIONS="${APP_HOME_PERMISSIONS}" \
APP_CERTS_DIRECTORY="${APP_CERTS_DIRECTORY}" \
APP_CERTS_DIRECTORY_PERMISSIONS="${APP_CERTS_DIRECTORY_PERMISSIONS}" \
APP_AUDIO_DIRECTORY="${APP_AUDIO_DIRECTORY}" \
APP_AUDIO_DIRECTORY_PERMISSIONS="${APP_AUDIO_DIRECTORY_PERMISSIONS}" \
APP_LOGS_DIRECTORY="${APP_LOGS_DIRECTORY}" \
APP_LOGS_DIRECTORY_PERMISSIONS="${APP_LOGS_DIRECTORY_PERMISSIONS}" \
bash << "EOF"

create_directory()
{
    local TARGET_DIRECTORY="$1"
    local TARGET_USER="$2"
    local TARGET_GROUP="$3"
    local TARGET_PERMISSIONS="$4"

    if [[ ! -d "${TARGET_DIRECTORY}" ]]; then
        echo "Creating ${TARGET_DIRECTORY}" \
             "with ${TARGET_PERMISSIONS} permissions."
        mkdir -p -m "${TARGET_PERMISSIONS}" "${TARGET_DIRECTORY}"
    else
        echo "${TARGET_DIRECTORY} already exists, skipping."
    fi
}

check_ownership()
{
    local TARGET="$1"
    local TARGET_USER="$2"
    local TARGET_GROUP="$3"

    echo "Checking ownership of ${TARGET}"
    local USER=$(stat -c "%U" "${TARGET}")
    local GROUP=$(stat -c "%G" "${TARGET}")
    if [[ ( "${USER}" != "${TARGET_USER}" ) || \
          ( "${GROUP}" != "${TARGET_GROUP}" ) ]]; then
        echo "Changing ownership of ${TARGET}" \
             "to ${TARGET_USER}:${TARGET_GROUP}"
        chown "${TARGET_USER}:${TARGET_GROUP}" "${TARGET}"
    else
        echo "Ownership of ${TARGET}" \
             "is already ${TARGET_USER}:${TARGET_GROUP}, skipping."
    fi
}

echo "Running as $(whoami)."

create_directory \
"${APP_HOME}" \
"${CURRENT_USER}" \
"${CURRENT_GROUP}" \
"${APP_HOME_PERMISSIONS}"

create_directory \
"${APP_CERTS_DIRECTORY}" \
"${CURRENT_USER}" \
"${CURRENT_GROUP}" \
"${APP_CERTS_DIRECTORY_PERMISSIONS}"

create_directory \
"${APP_AUDIO_DIRECTORY}" \
"${CURRENT_USER}" \
"${CURRENT_GROUP}" \
"${APP_AUDIO_DIRECTORY_PERMISSIONS}"

create_directory \
"${APP_LOGS_DIRECTORY}" \
"${CURRENT_USER}" \
"${CURRENT_GROUP}" \
"${APP_LOGS_DIRECTORY_PERMISSIONS}"

check_ownership \
"${APP_HOME}" \
"${CURRENT_USER}" \
"${CURRENT_GROUP}"

check_ownership \
"${APP_CERTS_DIRECTORY}" \
"${CURRENT_USER}" \
"${CURRENT_GROUP}"

check_ownership \
"${APP_AUDIO_DIRECTORY}" \
"${CURRENT_USER}" \
"${CURRENT_GROUP}"

check_ownership \
"${APP_LOGS_DIRECTORY}" \
"${CURRENT_USER}" \
"${CURRENT_GROUP}"

echo "Finished running as $(whoami)."

EOF

check_permissions()
{
    local TARGET="$1"
    local TARGET_PERMISSIONS="$2"

    echo "Checking permissions of ${TARGET}"
    local PERMISSIONS=$(stat -c "%a" "${TARGET}")
    if [[ "${PERMISSIONS}" != "${TARGET_PERMISSIONS}" ]]; then
        echo "Changing permissions of ${TARGET}" \
             "to ${TARGET_PERMISSIONS}"
        chmod "${TARGET_PERMISSIONS}" "${TARGET}"
    else
        echo "Permissions of ${TARGET}" \
             "are already ${TARGET_PERMISSIONS}, skipping."
    fi
}

echo "Running as $(whoami)."

check_permissions \
"${APP_HOME}" \
"${APP_HOME_PERMISSIONS}"

check_permissions \
"${APP_CERTS_DIRECTORY}" \
"${APP_CERTS_DIRECTORY_PERMISSIONS}"

check_permissions \
"${APP_AUDIO_DIRECTORY}" \
"${APP_AUDIO_DIRECTORY_PERMISSIONS}"

check_permissions \
"${APP_LOGS_DIRECTORY}" \
"${APP_LOGS_DIRECTORY_PERMISSIONS}"

ls -d -l "${APP_HOME}"
ls -d -l "${APP_CERTS_DIRECTORY}"
ls -d -l "${APP_AUDIO_DIRECTORY}"
ls -d -l "${APP_LOGS_DIRECTORY}"

echo "Finished running as $(whoami)."

echo