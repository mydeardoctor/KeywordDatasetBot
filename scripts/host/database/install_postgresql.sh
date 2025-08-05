#!/bin/bash

if [[ ( -z "${POSTGRESQL_MAJOR_VERSION}" ) || \
      ( -z "${DATABASE_ADMIN_USER}" ) || \
      ( -z "${DATABASE_ADMIN_GROUP}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

sudo \
env \
POSTGRESQL_MAJOR_VERSION="${POSTGRESQL_MAJOR_VERSION}" \
DATABASE_ADMIN_USER="${DATABASE_ADMIN_USER}" \
DATABASE_ADMIN_GROUP="${DATABASE_ADMIN_GROUP}" \
bash << "EOF"

get_is_postgresql_installed()
{
    dpkg-query -W -f '${db:Status-Abbrev}\n' \
    "postgresql-${POSTGRESQL_MAJOR_VERSION}" 2>/dev/null | grep -q "ii"
}

get_does_psql_command_exist()
{
    command -v psql >/dev/null 2>&1
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

if { ! get_is_postgresql_installed; } || \
   { ! get_does_psql_command_exist; }; then
    echo "Postgresql is not installed, installing."
    apt-get update
    apt-get -y install "postgresql-${POSTGRESQL_MAJOR_VERSION}"
else
    echo "Postgresql is already installed, skipping."
fi

psql --version

DATABASE_ADMIN_HOME=$(getent passwd "${DATABASE_ADMIN_USER}" | cut -d : -f 6)
DATABASE_DATA_DIRECTORY=\
"${DATABASE_ADMIN_HOME}/${POSTGRESQL_MAJOR_VERSION}/main"

check_ownership \
"${DATABASE_DATA_DIRECTORY}" \
"${DATABASE_ADMIN_USER}" \
"${DATABASE_ADMIN_GROUP}"

echo "Finished running as $(whoami)."

EOF

sudo -u "${DATABASE_ADMIN_USER}" \
env \
POSTGRESQL_MAJOR_VERSION="${POSTGRESQL_MAJOR_VERSION}" \
DATABASE_ADMIN_USER="${DATABASE_ADMIN_USER}" \
bash << "EOF"

DATABASE_ADMIN_HOME=$(getent passwd "${DATABASE_ADMIN_USER}" | cut -d : -f 6)
DATABASE_DATA_DIRECTORY=\
"${DATABASE_ADMIN_HOME}/${POSTGRESQL_MAJOR_VERSION}/main"
DATABASE_DATA_DIRECTORY_PERMISSIONS="700"

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
"${DATABASE_DATA_DIRECTORY}" \
"${DATABASE_DATA_DIRECTORY_PERMISSIONS}"

ls -d -l "${DATABASE_DATA_DIRECTORY}"

echo "Finished running as $(whoami)."

EOF

echo