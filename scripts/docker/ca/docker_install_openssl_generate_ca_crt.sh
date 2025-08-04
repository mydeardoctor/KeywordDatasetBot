#!/bin/bash

if [[ ( -z "${CA_ADMIN_USER}" ) ||
      ( -z "${CA_ADMIN_GROUP}" ) ||
      ( -z "${CA_ADMIN_HOME}" ) ||
      ( -z "${CA_KEY}" ) ||
      ( -z "${CA_KEY_PERMISSIONS}" ) ||
      ( -z "${CA_KEY_PASSWORD}" ) ||
      ( -z "${CA_CSR}" ) ||
      ( -z "${CA_CSR_PERMISSIONS}" ) ||
      ( -z "${CA_CRT}" ) ||
      ( -z "${CA_CRT_PERMISSIONS}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

CA_IMAGE_NAME="ca"

sudo docker build \
-t "${CA_IMAGE_NAME}" \
-f ./Dockerfile \
--build-arg CA_ADMIN_HOME="${CA_ADMIN_HOME}" \
--progress=plain \
.

BUILD_RESULT=$?
if [[ "${BUILD_RESULT}" -ne 0 ]]; then
    exit 1
fi

sudo docker run \
--name container_ca \
-e CA_ADMIN_USER="${CA_ADMIN_USER}" \
-e CA_ADMIN_GROUP="${CA_ADMIN_GROUP}" \
-e CA_ADMIN_HOME="${CA_ADMIN_HOME}" \
-e CA_KEY="${CA_KEY}" \
-e CA_KEY_PERMISSIONS="${CA_KEY_PERMISSIONS}" \
-e CA_KEY_PASSWORD="${CA_KEY_PASSWORD}" \
-e CA_CSR="${CA_CSR}" \
-e CA_CSR_PERMISSIONS="${CA_CSR_PERMISSIONS}" \
-e CA_CRT="${CA_CRT}" \
-e CA_CRT_PERMISSIONS="${CA_CRT_PERMISSIONS}" \
--mount "type=bind,src=${CA_ADMIN_HOME},dst=${CA_ADMIN_HOME}" \
--rm \
"${CA_IMAGE_NAME}"

RUN_RESULT=$?
if [[ "${RUN_RESULT}" -ne 0 ]]; then
    exit 1
fi