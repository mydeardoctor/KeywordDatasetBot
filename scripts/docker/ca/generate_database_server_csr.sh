#!/bin/bash

if [[ ( -z "${DATABASE_ADMIN_UID}" ) || \
      ( -z "${DATABASE_ADMIN_GID}") || \
      ( -z "${DATABASE_CERTS_DIRECTORY}" ) || \
      ( -z "${DATABASE_SERVER_KEY}" ) || \
      ( -z "${DATABASE_SERVER_KEY_PERMISSIONS}" ) || \
      ( -z "${DATABASE_SERVER_KEY_PASSWORD}" ) || \
      ( -z "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" ) || \
      ( -z "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD_PERMISSIONS}" ) || \
      ( -z "${DATABASE_SERVER_CSR}" ) || \
      ( -z "${DATABASE_SERVER_CSR_PERMISSIONS}" ) || \
      ( -z "${DATABASE_SERVER_CONF}" ) || \
      ( -z "${DATABASE_SERVER_ALTERNATE_HOSTNAME}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
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

echo "Changing directory to ${DATABASE_CERTS_DIRECTORY}"
cd "${DATABASE_CERTS_DIRECTORY}"

if [[ ! -f "${DATABASE_SERVER_KEY}" ]]; then
    echo "Generating ${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_KEY}"
    openssl genpkey \
    -algorithm RSA \
    -AES-256-CBC \
    -pass env:DATABASE_SERVER_KEY_PASSWORD \
    -out "${DATABASE_SERVER_KEY}" \
    -quiet
else
    echo "${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_KEY}" \
         "already exists, skipping."
fi

if [[ ! -f "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" ]]; then
    echo \
    "Generating" \
    "${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}"
    openssl pkey \
    -in "${DATABASE_SERVER_KEY}" \
    -passin env:DATABASE_SERVER_KEY_PASSWORD \
    -out "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}"
else
    echo "${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" \
         "already exists, skipping."
fi

if [[ ! -f "${DATABASE_SERVER_CONF}" ]]; then
    echo "Generating ${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CONF}"

cat > "${DATABASE_SERVER_CONF}" << EOT
[req]
req_extensions=v3_req

[v3_req]
subjectAltName=@subject_alt_names

[subject_alt_names]
IP.1 = 127.0.0.1
IP.2 = ::1
DNS.1 = localhost
DNS.2 = ${DATABASE_SERVER_ALTERNATE_HOSTNAME}
EOT

else
    echo "${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CONF}" \
         "already exists, skipping."
fi

if [[ ! -f "${DATABASE_SERVER_CSR}" ]]; then
    echo "Generating ${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CSR}"
    # CN must match one of database server's hostnames for ssl full verification.
    openssl req \
    -new \
    -key "${DATABASE_SERVER_KEY}" \
    -passin env:DATABASE_SERVER_KEY_PASSWORD \
    -subj "/O=my_dear_doctor/OU=database/CN=${DATABASE_SERVER_ALTERNATE_HOSTNAME}" \
    -config "${DATABASE_SERVER_CONF}" \
    -out "${DATABASE_SERVER_CSR}"
else
    echo "${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CSR}" \
         "already exists, skipping."
fi

if [[ -f "${DATABASE_SERVER_CONF}" ]]; then
    echo "Removing ${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CONF}"
    rm "${DATABASE_SERVER_CONF}"
fi

check_ownership \
"${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_KEY}" \
"${DATABASE_ADMIN_UID}" \
"${DATABASE_ADMIN_GID}"

check_ownership \
"${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" \
"${DATABASE_ADMIN_UID}" \
"${DATABASE_ADMIN_GID}"

check_ownership \
"${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CSR}" \
"${DATABASE_ADMIN_UID}" \
"${DATABASE_ADMIN_GID}"

check_permissions \
"${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_KEY}" \
"${DATABASE_SERVER_KEY_PERMISSIONS}"

check_permissions \
"${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" \
"${DATABASE_SERVER_KEY_WITHOUT_PASSWORD_PERMISSIONS}"

check_permissions \
"${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CSR}" \
"${DATABASE_SERVER_CSR_PERMISSIONS}"

ls -l "${DATABASE_SERVER_KEY}"
ls -l "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}"
ls -l "${DATABASE_SERVER_CSR}"

echo