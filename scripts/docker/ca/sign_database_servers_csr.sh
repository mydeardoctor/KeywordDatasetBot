#!/bin/bash

if [[ ( -z "${CA_ADMIN_USER}" ) || \
      ( -z "${CA_ADMIN_GROUP}" ) || \
      ( -z "${CA_ADMIN_HOME}" ) || \
      ( -z "${CA_KEY}" ) || \
      ( -z "${CA_KEY_PASSWORD}" ) || \
      ( -z "${CA_CRT}" ) || \
      ( -z "${DATABASE_CERTS_DIRECTORY}" ) || \
      ( -z "${DATABASE_SERVER_CSR}" ) || \
      ( -z "${DATABASE_SERVER_CRT}" ) || \
      ( -z "${DATABASE_SERVER_CRT_PERMISSIONS}" ) || \
      ( -z "${DATABASE_SERVER_CONF}" ) || \
      ( -z "${DATABASE_SERVER_CONF_PERMISSIONS}" ) || \
      ( -z "${DATABASE_SERVER_ALTERNATE_HOSTNAME}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

run_or_exit()
{
    "$@"
    local RESULT=$?
    if [[ "${RESULT}" -ne 0 ]]; then
        exit 1
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

echo "Changing directory to ${CA_ADMIN_HOME}"
cd "${CA_ADMIN_HOME}"

if [[ -f "${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CSR}" ]]; then
    echo "Moving ${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CSR}" \
         "to ${CA_ADMIN_HOME}"
    mv -f \
    "${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CSR}" \
    "${CA_ADMIN_HOME}"
else
    echo "${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CSR}" \
         "does not exist!" >&2
    exit 1
fi

if [[ ! -f "${DATABASE_SERVER_CONF}" ]]; then
    echo "Generating ${CA_ADMIN_HOME}/${DATABASE_SERVER_CONF}" \
         "with ${CA_ADMIN_USER}:${CA_ADMIN_GROUP} ownership."

cat > "${DATABASE_SERVER_CONF}" << EOT
# Default section
extensions=usr_cert

[usr_cert]
subjectAltName=@subject_alt_names

[subject_alt_names]
IP.1 = 127.0.0.1
IP.2 = ::1
DNS.1 = localhost
DNS.2 = ${DATABASE_SERVER_ALTERNATE_HOSTNAME}
EOT

else
    echo "${CA_ADMIN_HOME}/${DATABASE_SERVER_CONF} already exists, skipping."

    check_ownership \
    "${CA_ADMIN_HOME}/${DATABASE_SERVER_CONF}" \
    "${CA_ADMIN_USER}" \
    "${CA_ADMIN_GROUP}"
fi

check_permissions \
"${CA_ADMIN_HOME}/${DATABASE_SERVER_CONF}" \
"${DATABASE_SERVER_CONF_PERMISSIONS}"

if [[ ! -f "${DATABASE_SERVER_CRT}" ]]; then
    echo "Generating ${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}" \
         "with ${CA_ADMIN_USER}:${CA_ADMIN_GROUP} ownership."
    openssl x509 \
    -req \
    -in "${DATABASE_SERVER_CSR}" \
    -sha256 \
    -CAkey "${CA_KEY}" \
    -passin env:CA_KEY_PASSWORD \
    -CA "${CA_CRT}" \
    -CAcreateserial \
    -extfile "${DATABASE_SERVER_CONF}" \
    -out "${DATABASE_SERVER_CRT}" \
    -days 365
else
    echo "${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT} already exists, skipping."

    check_ownership \
    "${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}" \
    "${CA_ADMIN_USER}" \
    "${CA_ADMIN_GROUP}"
fi

check_permissions \
"${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}" \
"${DATABASE_SERVER_CRT_PERMISSIONS}"

echo "Verifying ${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}"
run_or_exit openssl verify \
-CAfile "${CA_CRT}" \
"${DATABASE_SERVER_CRT}"

if [[ -f "${DATABASE_SERVER_CSR}" ]]; then
    echo "Removing ${CA_ADMIN_HOME}/${DATABASE_SERVER_CSR}"
    rm "${DATABASE_SERVER_CSR}"
fi

if [[ -f "${DATABASE_SERVER_CONF}" ]]; then
    echo "Removing ${CA_ADMIN_HOME}/${DATABASE_SERVER_CONF}"
    rm "${DATABASE_SERVER_CONF}"
fi

echo "Moving ${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}" \
     "to ${DATABASE_CERTS_DIRECTORY}"
mv -f \
"${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}" \
"${DATABASE_CERTS_DIRECTORY}"

if [[ -f "${CA_CRT}" ]]; then
    echo "Copying ${CA_ADMIN_HOME}/${CA_CRT} to ${DATABASE_CERTS_DIRECTORY}"
    cp -p -f "${CA_CRT}" "${DATABASE_CERTS_DIRECTORY}"
else
    echo "${CA_ADMIN_HOME}/${CA_CRT} does not exist!" >&2
    exit 1
fi

ls -l "${DATABASE_CERTS_DIRECTORY}/${DATABASE_SERVER_CRT}"
ls -l "${DATABASE_CERTS_DIRECTORY}/${CA_CRT}"

echo