#!/bin/bash

if [[ ( -z "${DATABASE_ADMIN_USER}" ) || \
      ( -z "${DATABASE_ADMIN_GROUP}" ) || \
      ( -z "${DATABASE_ADMIN_HOME}" ) || \
      ( -z "${DATABASE_CERTS_DIRECTORY}" )]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

DATABASE_ADMIN_HOME_PERMISSIONS="700"
DATABASE_CERTS_DIRECTORY_PERMISSIONS="700"

sudo \
env \
DATABASE_ADMIN_USER="${DATABASE_ADMIN_USER}" \
DATABASE_ADMIN_GROUP="${DATABASE_ADMIN_GROUP}" \
DATABASE_ADMIN_HOME="${DATABASE_ADMIN_HOME}" \
DATABASE_ADMIN_HOME_PERMISSIONS="${DATABASE_ADMIN_HOME_PERMISSIONS}" \
DATABASE_CERTS_DIRECTORY="${DATABASE_CERTS_DIRECTORY}" \
DATABASE_CERTS_DIRECTORY_PERMISSIONS="${DATABASE_CERTS_DIRECTORY_PERMISSIONS}" \
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

if ! id ${DATABASE_ADMIN_USER} 1>/dev/null 2>&1; then
    echo "Creating ${DATABASE_ADMIN_USER}"
    adduser \
    --system \
    --group \
    --comment "PostgreSQL administrator" \
    --home "${DATABASE_ADMIN_HOME}" \
    --shell /bin/bash \
    "${DATABASE_ADMIN_USER}"
else
    echo "${DATABASE_ADMIN_USER} user already exists, skipping."
fi

getent passwd "${DATABASE_ADMIN_USER}"
passwd -S "${DATABASE_ADMIN_USER}"

create_directory \
"${DATABASE_ADMIN_HOME}" \
"${DATABASE_ADMIN_USER}" \
"${DATABASE_ADMIN_GROUP}" \
"${DATABASE_ADMIN_HOME_PERMISSIONS}"

create_directory \
"${DATABASE_CERTS_DIRECTORY}" \
"${DATABASE_ADMIN_USER}" \
"${DATABASE_ADMIN_GROUP}" \
"${DATABASE_CERTS_DIRECTORY_PERMISSIONS}"

check_ownership \
"${DATABASE_ADMIN_HOME}" \
"${DATABASE_ADMIN_USER}" \
"${DATABASE_ADMIN_GROUP}"

check_ownership \
"${DATABASE_CERTS_DIRECTORY}" \
"${DATABASE_ADMIN_USER}" \
"${DATABASE_ADMIN_GROUP}"

echo "Finished running as $(whoami)."

EOF

sudo -u "${DATABASE_ADMIN_USER}" \
env \
DATABASE_ADMIN_HOME="${DATABASE_ADMIN_HOME}" \
DATABASE_ADMIN_HOME_PERMISSIONS="${DATABASE_ADMIN_HOME_PERMISSIONS}" \
DATABASE_CERTS_DIRECTORY="${DATABASE_CERTS_DIRECTORY}" \
DATABASE_CERTS_DIRECTORY_PERMISSIONS="${DATABASE_CERTS_DIRECTORY_PERMISSIONS}" \
bash << "EOF"

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
"${DATABASE_ADMIN_HOME}" \
"${DATABASE_ADMIN_HOME_PERMISSIONS}"

check_permissions \
"${DATABASE_CERTS_DIRECTORY}" \
"${DATABASE_CERTS_DIRECTORY_PERMISSIONS}"

ls -d -l "${DATABASE_ADMIN_HOME}"
ls -d -l "${DATABASE_CERTS_DIRECTORY}"

echo "Finished running as $(whoami)."

EOF

echo