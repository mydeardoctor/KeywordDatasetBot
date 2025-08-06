#!/bin/bash

if [[ ( -z "${CA_ADMIN_USER}" ) || \
      ( -z "${CA_ADMIN_GROUP}" ) || \
      ( -z "${CA_ADMIN_HOME}" ) || \
      ( -z "${CA_KEY}" ) || \
      ( -z "${CA_KEY_PERMISSIONS}" ) || \
      ( -z "${CA_KEY_PASSWORD}" ) || \
      ( -z "${CA_CSR}" ) || \
      ( -z "${CA_CSR_PERMISSIONS}" ) || \
      ( -z "${CA_CRT}" ) || \
      ( -z "${CA_CRT_PERMISSIONS}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

sudo \
env \
CA_ADMIN_USER="${CA_ADMIN_USER}" \
CA_ADMIN_GROUP="${CA_ADMIN_GROUP}" \
CA_ADMIN_HOME="${CA_ADMIN_HOME}" \
CA_KEY="${CA_KEY}" \
CA_KEY_PERMISSIONS="${CA_KEY_PERMISSIONS}" \
CA_KEY_PASSWORD="${CA_KEY_PASSWORD}" \
CA_CSR="${CA_CSR}" \
CA_CSR_PERMISSIONS="${CA_CSR_PERMISSIONS}" \
CA_CRT="${CA_CRT}" \
CA_CRT_PERMISSIONS="${CA_CRT_PERMISSIONS}" \
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

OPENSSL_INSTALLATION_DIRECTORY="$(openssl version -d | cut -d '"' -f 2)"
OPENSSL_CNF="${OPENSSL_INSTALLATION_DIRECTORY}/openssl.cnf"

echo "Running as $(whoami)."
echo "Changing directory to ${CA_ADMIN_HOME}"
cd "${CA_ADMIN_HOME}"

if [[ ! -f "${CA_KEY}" ]]; then
    echo "Generating ${CA_ADMIN_HOME}/${CA_KEY}" \
         "with ${CA_ADMIN_USER}:${CA_ADMIN_GROUP} ownership."
    openssl genpkey \
    -algorithm RSA \
    -AES-256-CBC \
    -pass env:CA_KEY_PASSWORD \
    -out "${CA_KEY}" \
    -quiet
else
    echo "${CA_ADMIN_HOME}/${CA_KEY} already exists, skipping."

    check_ownership \
    "${CA_ADMIN_HOME}/${CA_KEY}" \
    "${CA_ADMIN_USER}" \
    "${CA_ADMIN_GROUP}"
fi

check_permissions "${CA_ADMIN_HOME}/${CA_KEY}" "${CA_KEY_PERMISSIONS}"

if [[ ! -f "${CA_CSR}" ]]; then
    echo "Generating ${CA_ADMIN_HOME}/${CA_CSR}" \
         "with ${CA_ADMIN_USER}:${CA_ADMIN_GROUP} ownership."
    openssl req \
    -new \
    -key "${CA_KEY}" \
    -passin env:CA_KEY_PASSWORD \
    -subj "/O=my_dear_doctor/OU=ca/CN=ca_admin" \
    -out "${CA_CSR}"
else
    echo "${CA_ADMIN_HOME}/${CA_CSR} already exists, skipping."

    check_ownership \
    "${CA_ADMIN_HOME}/${CA_CSR}" \
    "${CA_ADMIN_USER}" \
    "${CA_ADMIN_GROUP}"
fi

check_permissions "${CA_ADMIN_HOME}/${CA_CSR}" "${CA_CSR_PERMISSIONS}"

if [[ ! -f "${CA_CRT}" ]]; then
    echo "Generating ${CA_ADMIN_HOME}/${CA_CRT}" \
         "with ${CA_ADMIN_USER}:${CA_ADMIN_GROUP} ownership."
    openssl x509 \
    -req \
    -in "${CA_CSR}" \
    -sha256 \
    -key "${CA_KEY}" \
    -passin env:CA_KEY_PASSWORD \
    -extfile "${OPENSSL_CNF}" \
    -extensions v3_ca \
    -out "${CA_CRT}" \
    -days 365
else
    echo "${CA_ADMIN_HOME}/${CA_CRT} already exists, skipping."

    check_ownership \
    "${CA_ADMIN_HOME}/${CA_CRT}" \
    "${CA_ADMIN_USER}" \
    "${CA_ADMIN_GROUP}"
fi

check_permissions "${CA_ADMIN_HOME}/${CA_CRT}" "${CA_CRT_PERMISSIONS}"

if [[ -f "${CA_CSR}" ]]; then
    echo "Removing ${CA_ADMIN_HOME}/${CA_CSR}"
    rm "${CA_CSR}"
fi

ls -l "${CA_KEY}"
ls -l "${CA_CRT}"

echo "Finished running as $(whoami)."

EOF

echo