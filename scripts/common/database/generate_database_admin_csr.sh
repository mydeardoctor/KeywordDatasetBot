#!/bin/bash

if [[ "$(id -u)" -ne 0 ]]; then
    echo "Not running as root. Re-running as root."
    exec sudo -E "$0" "$@"
fi

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
echo "Changing directory to ${DATABASE_CERTS_DIRECTORY}"
cd "${DATABASE_CERTS_DIRECTORY}"

if [[ ! -f "${DATABASE_ADMIN_KEY}" ]]; then
    echo "Generating ${DATABASE_CERTS_DIRECTORY}/${DATABASE_ADMIN_KEY}"
    openssl genpkey \
    -algorithm RSA \
    -AES-256-CBC \
    -pass env:DATABASE_ADMIN_KEY_PASSWORD \
    -out "${DATABASE_ADMIN_KEY}" \
    -quiet
else
    echo "${DATABASE_CERTS_DIRECTORY}/${DATABASE_ADMIN_KEY}" \
         "already exists, skipping."
fi

if [[ ! -f "${DATABASE_ADMIN_CSR}" ]]; then
    echo "Generating ${DATABASE_CERTS_DIRECTORY}/${DATABASE_ADMIN_CSR}"
    # CN must match client's database role for ssl full verification.
    openssl req \
    -new \
    -key "${DATABASE_ADMIN_KEY}" \
    -passin env:DATABASE_ADMIN_KEY_PASSWORD \
    -subj "/O=my_dear_doctor/OU=database/CN=${DATABASE_ADMIN_USER}" \
    -out "${DATABASE_ADMIN_CSR}"
else
    echo "${DATABASE_CERTS_DIRECTORY}/${DATABASE_ADMIN_CSR}" \
         "already exists, skipping."
fi

check_ownership \
"${DATABASE_CERTS_DIRECTORY}/${DATABASE_ADMIN_KEY}" \
"${DATABASE_ADMIN_UID}" \
"${DATABASE_ADMIN_GID}"

check_ownership \
"${DATABASE_CERTS_DIRECTORY}/${DATABASE_ADMIN_CSR}" \
"${DATABASE_ADMIN_UID}" \
"${DATABASE_ADMIN_GID}"

check_permissions \
"${DATABASE_CERTS_DIRECTORY}/${DATABASE_ADMIN_KEY}" \
"${DATABASE_ADMIN_KEY_PERMISSIONS}"

check_permissions \
"${DATABASE_CERTS_DIRECTORY}/${DATABASE_ADMIN_CSR}" \
"${DATABASE_ADMIN_CSR_PERMISSIONS}"

ls -l "${DATABASE_ADMIN_KEY}"
ls -l "${DATABASE_ADMIN_CSR}"

echo "Finished running as $(whoami)."

echo