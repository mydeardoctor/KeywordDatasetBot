#!/bin/bash

if [[ ( -z "${CA_ADMIN_USER}" ) || \
      ( -z "${CA_ADMIN_GROUP}" ) || \
      ( -z "${CA_ADMIN_HOME}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

CA_ADMIN_HOME_PERMISSIONS="700"

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

if [[ ! -d "${CA_ADMIN_HOME}" ]]; then
    echo "Creating ${CA_ADMIN_HOME}" \
         "with ${CA_ADMIN_USER}:${CA_ADMIN_GROUP} ownership" \
         "and ${CA_ADMIN_HOME_PERMISSIONS} permissions."
    sudo mkdir -p -m "${CA_ADMIN_HOME_PERMISSIONS}" "${CA_ADMIN_HOME}"

else
    echo "${CA_ADMIN_HOME} already exists, skipping."

    check_ownership "${CA_ADMIN_HOME}" "${CA_ADMIN_USER}" "${CA_ADMIN_GROUP}"
    check_permissions "${CA_ADMIN_HOME}" "${CA_ADMIN_HOME_PERMISSIONS}"
fi

ls -d -l "${CA_ADMIN_HOME}"
echo