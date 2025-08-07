#!/bin/bash

if [[ ( -z "${POSTGRESQL_MAJOR_VERSION}" ) || \
      ( -z "${POSTGRESQL_MINOR_VERSION}" ) || \
      ( -z "${DATABASE_ADMIN_HOME}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

#sudo \
#env \
#POSTGRESQL_MAJOR_VERSION="${POSTGRESQL_MAJOR_VERSION}" \
#POSTGRESQL_MINOR_VERSION="${POSTGRESQL_MINOR_VERSION}" \
#bash << "EOF"

#DATABASE_ADMIN_UID=$(id -u "${DATABASE_ADMIN_USER}")
#DATABASE_ADMIN_GID=$(id -g "${DATABASE_ADMIN_USER}")
IMAGE_NAME="database_server"

run_or_exit()
{
    "$@"
    local RESULT=$?
    if [[ "${RESULT}" -ne 0 ]]; then
        exit 1
    fi
}

echo "Running as $(whoami)."

sudo docker build \
-t "${IMAGE_NAME}" \
-f ./Dockerfile \
--build-arg POSTGRESQL_MAJOR_VERSION="${POSTGRESQL_MAJOR_VERSION}" \
--build-arg POSTGRESQL_MINOR_VERSION="${POSTGRESQL_MINOR_VERSION}" \
--progress=plain \
.

sudo docker run \
-it \
--name container_database_server \
--mount "type=bind,src=${DATABASE_ADMIN_HOME},dst=/var/lib/postgresql" \
--rm \
"${IMAGE_NAME}"

#/var/lib/postgresql_docker

echo "Finished running as $(whoami)."

#EOF

echo

#-e CA_ADMIN_USER="${CA_ADMIN_USER}" \