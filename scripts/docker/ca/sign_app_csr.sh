#!/bin/bash

if [[ ( -z "${CA_ADMIN_USER}" ) || \
      ( -z "${CA_ADMIN_GROUP}" ) || \
      ( -z "${CA_ADMIN_HOME}" ) || \
      ( -z "${CA_KEY}" ) || \
      ( -z "${CA_KEY_PASSWORD}" ) || \
      ( -z "${CA_CRT}" ) || \
      ( -z "${APP_CERTS_DIRECTORY}" ) || \
      ( -z "${APP_CSR}" ) || \
      ( -z "${APP_CRT}" ) || \
      ( -z "${APP_CRT_PERMISSIONS}" ) ]]; then
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

if [[ -f "${APP_CERTS_DIRECTORY}/${APP_CSR}" ]]; then
    echo "Moving ${APP_CERTS_DIRECTORY}/${APP_CSR}" \
         "to ${CA_ADMIN_HOME}"
    mv -f \
    "${APP_CERTS_DIRECTORY}/${APP_CSR}" \
    "${CA_ADMIN_HOME}"
else
    echo "${APP_CERTS_DIRECTORY}/${APP_CSR}" \
         "does not exist!" >&2
    exit 1
fi

if [[ ! -f "${APP_CRT}" ]]; then
    echo "Generating ${CA_ADMIN_HOME}/${APP_CRT}" \
         "with ${CA_ADMIN_USER}:${CA_ADMIN_GROUP} ownership."
    openssl x509 \
    -req \
    -in "${APP_CSR}" \
    -sha256 \
    -CAkey "${CA_KEY}" \
    -passin env:CA_KEY_PASSWORD \
    -CA "${CA_CRT}" \
    -CAcreateserial \
    -out "${APP_CRT}" \
    -days 365
else
    echo "${CA_ADMIN_HOME}/${APP_CRT} already exists, skipping."

    check_ownership \
    "${CA_ADMIN_HOME}/${APP_CRT}" \
    "${CA_ADMIN_USER}" \
    "${CA_ADMIN_GROUP}"
fi

check_permissions \
"${CA_ADMIN_HOME}/${APP_CRT}" \
"${APP_CRT_PERMISSIONS}"

echo "Verifying ${CA_ADMIN_HOME}/${APP_CRT}"
run_or_exit openssl verify \
-CAfile "${CA_CRT}" \
"${APP_CRT}"

if [[ -f "${APP_CSR}" ]]; then
    echo "Removing ${CA_ADMIN_HOME}/${APP_CSR}"
    rm "${APP_CSR}"
fi

echo "Moving ${CA_ADMIN_HOME}/${APP_CRT}" \
     "to ${APP_CERTS_DIRECTORY}"
mv -f \
"${CA_ADMIN_HOME}/${APP_CRT}" \
"${APP_CERTS_DIRECTORY}"

if [[ -f "${CA_CRT}" ]]; then
    echo "Copying ${CA_ADMIN_HOME}/${CA_CRT} to ${APP_CERTS_DIRECTORY}"
    cp -p -f "${CA_CRT}" "${APP_CERTS_DIRECTORY}"
else
    echo "${CA_ADMIN_HOME}/${CA_CRT} does not exist!" >&2
    exit 1
fi

ls -l "${APP_CERTS_DIRECTORY}/${APP_CRT}"
ls -l "${APP_CERTS_DIRECTORY}/${CA_CRT}"

echo