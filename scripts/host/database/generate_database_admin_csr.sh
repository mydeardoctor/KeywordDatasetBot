#!/bin/bash

if [[ ( -z "${POSTGRESQL_MAJOR_VERSION}" ) || \
      ( -z "${DATABASE_ADMIN_USER}" ) || \
      ( -z "${DATABASE_ADMIN_GROUP}" ) || \
      ( -z "${DATABASE_ADMIN_KEY}" ) || \
      ( -z "${DATABASE_ADMIN_KEY_PERMISSIONS}" ) || \
      ( -z "${DATABASE_ADMIN_KEY_PASSWORD}" ) || \
      ( -z "${DATABASE_ADMIN_CSR}" ) || \
      ( -z "${DATABASE_ADMIN_CSR_PERMISSIONS}" ) || \
      ( -z "${DATABASE_ADMIN_CRT}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

sudo -u "${DATABASE_ADMIN_USER}" \
env \
POSTGRESQL_MAJOR_VERSION="${POSTGRESQL_MAJOR_VERSION}" \
DATABASE_ADMIN_USER="${DATABASE_ADMIN_USER}" \
DATABASE_ADMIN_GROUP="${DATABASE_ADMIN_GROUP}" \
DATABASE_ADMIN_KEY="${DATABASE_ADMIN_KEY}" \
DATABASE_ADMIN_KEY_PASSWORD="${DATABASE_ADMIN_KEY_PASSWORD}" \
DATABASE_ADMIN_CSR="${DATABASE_ADMIN_CSR}" \
DATABASE_ADMIN_CRT="${DATABASE_ADMIN_CRT}" \
bash << "EOF"

echo "Running as $(whoami)."
DATABASE_ADMIN_HOME="${HOME}"
DATABASE_DATA_DIRECTORY=\
"${DATABASE_ADMIN_HOME}/${POSTGRESQL_MAJOR_VERSION}/main"
echo "Changing directory to ${DATABASE_DATA_DIRECTORY}"
cd "${DATABASE_DATA_DIRECTORY}"

if [[ ! -f "${DATABASE_ADMIN_KEY}" ]]; then
    echo "Generating ${DATABASE_DATA_DIRECTORY}/${DATABASE_ADMIN_KEY}" \
         "with ${DATABASE_ADMIN_USER}:${DATABASE_ADMIN_GROUP} ownership."
    openssl genpkey \
    -algorithm RSA \
    -AES-256-CBC \
    -pass env:DATABASE_ADMIN_KEY_PASSWORD \
    -out "${DATABASE_ADMIN_KEY}" \
    -quiet
else
    echo "${DATABASE_DATA_DIRECTORY}/${DATABASE_ADMIN_KEY}" \
         "already exists, skipping."
fi

if [[ ( ! -f "${DATABASE_ADMIN_CSR}" ) && \
      ( ! -f "${DATABASE_ADMIN_CRT}" ) ]]; then
    echo "Generating ${DATABASE_DATA_DIRECTORY}/${DATABASE_ADMIN_CSR}" \
         "with ${DATABASE_ADMIN_USER}:${DATABASE_ADMIN_GROUP} ownership."
    # CN must match client's database role for ssl full verification.
    openssl req \
    -new \
    -key "${DATABASE_ADMIN_KEY}" \
    -passin env:DATABASE_ADMIN_KEY_PASSWORD \
    -subj "/O=my_dear_doctor/OU=database/CN=${DATABASE_ADMIN_USER}" \
    -out "${DATABASE_ADMIN_CSR}"
else
    echo "${DATABASE_DATA_DIRECTORY}/${DATABASE_ADMIN_CSR}" \
         "already exists, skipping."
fi

echo "Finished running as $(whoami)."

EOF

sudo \
env \
POSTGRESQL_MAJOR_VERSION="${POSTGRESQL_MAJOR_VERSION}" \
DATABASE_ADMIN_USER="${DATABASE_ADMIN_USER}" \
DATABASE_ADMIN_GROUP="${DATABASE_ADMIN_GROUP}" \
DATABASE_ADMIN_KEY="${DATABASE_ADMIN_KEY}" \
DATABASE_ADMIN_CSR="${DATABASE_ADMIN_CSR}" \
bash << "EOF"

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
DATABASE_ADMIN_HOME=$(getent passwd "${DATABASE_ADMIN_USER}" | cut -d : -f 6)
DATABASE_DATA_DIRECTORY=\
"${DATABASE_ADMIN_HOME}/${POSTGRESQL_MAJOR_VERSION}/main"
echo "Changing directory to ${DATABASE_DATA_DIRECTORY}"
cd "${DATABASE_DATA_DIRECTORY}"

check_ownership \
"${DATABASE_DATA_DIRECTORY}/${DATABASE_ADMIN_KEY}" \
"${DATABASE_ADMIN_USER}" \
"${DATABASE_ADMIN_GROUP}"

check_ownership \
"${DATABASE_DATA_DIRECTORY}/${DATABASE_ADMIN_CSR}" \
"${DATABASE_ADMIN_USER}" \
"${DATABASE_ADMIN_GROUP}"

echo "Finished running as $(whoami)."

EOF

sudo -u "${DATABASE_ADMIN_USER}" \
env \
POSTGRESQL_MAJOR_VERSION="${POSTGRESQL_MAJOR_VERSION}" \
DATABASE_ADMIN_KEY="${DATABASE_ADMIN_KEY}" \
DATABASE_ADMIN_KEY_PERMISSIONS="${DATABASE_ADMIN_KEY_PERMISSIONS}" \
DATABASE_ADMIN_CSR="${DATABASE_ADMIN_CSR}" \
DATABASE_ADMIN_CSR_PERMISSIONS="${DATABASE_ADMIN_CSR_PERMISSIONS}" \
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
DATABASE_ADMIN_HOME="${HOME}"
DATABASE_DATA_DIRECTORY=\
"${DATABASE_ADMIN_HOME}/${POSTGRESQL_MAJOR_VERSION}/main"
echo "Changing directory to ${DATABASE_DATA_DIRECTORY}"
cd "${DATABASE_DATA_DIRECTORY}"

check_permissions \
"${DATABASE_DATA_DIRECTORY}/${DATABASE_ADMIN_KEY}" \
"${DATABASE_ADMIN_KEY_PERMISSIONS}"

check_permissions \
"${DATABASE_DATA_DIRECTORY}/${DATABASE_ADMIN_CSR}" \
"${DATABASE_ADMIN_CSR_PERMISSIONS}"

ls -l "${DATABASE_ADMIN_KEY}"
ls -l "${DATABASE_ADMIN_CSR}"

echo "Finished running as $(whoami)."

EOF

echo