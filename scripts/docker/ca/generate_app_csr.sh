#!/bin/bash

if [[ ( -z "${APP_NAME}" ) || \
      ( -z "${APP_USER_UID}" ) || \
      ( -z "${APP_USER_GID}" ) || \
      ( -z "${APP_CERTS_DIRECTORY}" ) || \
      ( -z "${APP_KEY}") || \
      ( -z "${APP_KEY_PERMISSIONS}" ) || \
      ( -z "${APP_DER_KEY}" ) || \
      ( -z "${APP_DER_KEY_PERMISSIONS}" ) || \
      ( -z "${APP_KEY_PASSWORD}" ) || \
      ( -z "${APP_CSR}" ) || \
      ( -z "${APP_CSR_PERMISSIONS}" ) ]]; then
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

echo "Changing directory to ${APP_CERTS_DIRECTORY}"
cd "${APP_CERTS_DIRECTORY}"

if [[ ! -f "${APP_KEY}" ]]; then
    echo "Generating ${APP_CERTS_DIRECTORY}/${APP_KEY}"
    openssl genpkey \
    -algorithm RSA \
    -AES-256-CBC \
    -pass env:APP_KEY_PASSWORD \
    -out ${APP_KEY} \
    -quiet
else
    echo "${APP_CERTS_DIRECTORY}/${APP_KEY} already exists, skipping."
fi

if [[ ! -f "${APP_DER_KEY}" ]]; then
    echo "Generating ${APP_CERTS_DIRECTORY}/${APP_DER_KEY}"
    openssl pkcs8 \
    -topk8 \
    -inform PEM \
    -in "${APP_KEY}" \
    -passin env:APP_KEY_PASSWORD \
    -outform DER \
    -out "${APP_DER_KEY}" \
    -passout env:APP_KEY_PASSWORD
else
    echo "${APP_CERTS_DIRECTORY}/${APP_DER_KEY} already exists, skipping."
fi

echo "Verifying ${APP_CERTS_DIRECTORY}/${APP_DER_KEY}"
run_or_exit openssl pkcs8 \
-inform DER \
-in "${APP_DER_KEY}" \
-passin env:APP_KEY_PASSWORD \
-out /dev/null

if [[ ! -f "${APP_CSR}" ]]; then
    echo "Generating ${APP_CERTS_DIRECTORY}/${APP_CSR}"
    # CN must match client's database role for ssl full verification.
    openssl req \
    -new \
    -key "${APP_KEY}" \
    -passin env:APP_KEY_PASSWORD \
    -subj "/O=my_dear_doctor/OU=app/CN=${APP_NAME}" \
    -out "${APP_CSR}"
else
    echo "${APP_CERTS_DIRECTORY}/${APP_CSR} already exists, skipping."
fi

check_ownership \
"${APP_CERTS_DIRECTORY}/${APP_KEY}" \
"${APP_USER_UID}" \
"${APP_USER_GID}"

check_ownership \
"${APP_CERTS_DIRECTORY}/${APP_DER_KEY}" \
"${APP_USER_UID}" \
"${APP_USER_GID}"

check_ownership \
"${APP_CERTS_DIRECTORY}/${APP_CSR}" \
"${APP_USER_UID}" \
"${APP_USER_GID}"

check_permissions \
"${APP_CERTS_DIRECTORY}/${APP_KEY}" \
"${APP_KEY_PERMISSIONS}"

check_permissions \
"${APP_CERTS_DIRECTORY}/${APP_DER_KEY}" \
"${APP_DER_KEY_PERMISSIONS}"

check_permissions \
"${APP_CERTS_DIRECTORY}/${APP_CSR}" \
"${APP_CSR_PERMISSIONS}"

ls -l "${APP_KEY}"
ls -l "${APP_DER_KEY}"
ls -l "${APP_CSR}"

echo