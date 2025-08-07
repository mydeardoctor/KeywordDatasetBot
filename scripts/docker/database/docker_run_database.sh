#!/bin/bash




if [[ ( -z "${CA_CRT}" ) || \
      ( -z "${POSTGRESQL_MAJOR_VERSION}" ) || \
      ( -z "${POSTGRESQL_MINOR_VERSION}" ) || \
      ( -z "${DATABASE_ADMIN_USER}" ) || \
      ( -z "${DATABASE_ADMIN_ROLE_PASSWORD}" ) || \
      ( -z "${DATABASE_ADMIN_HOME}" ) || \
      ( -z "${DATABASE_ADMIN_KEY}" ) || \
      ( -z "${DATABASE_ADMIN_KEY_PASSWORD}" ) || \
      ( -z "${DATABASE_ADMIN_CRT}" ) || \
      ( -z "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" ) || \
      ( -z "${DATABASE_SERVER_CRT}" ) || \
      ( -z "${DATABASE_NAME}" ) || \
      ( -z "${APP_ROLE}" ) || \
      ( -z "${APP_ROLE_PASSWORD}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

#sudo \
#env \
#POSTGRESQL_MAJOR_VERSION="${POSTGRESQL_MAJOR_VERSION}" \
#POSTGRESQL_MINOR_VERSION="${POSTGRESQL_MINOR_VERSION}" \
#bash << "EOF"

DATABASE_ADMIN_UID=$(id -u "${DATABASE_ADMIN_USER}")
DATABASE_ADMIN_GID=$(id -g "${DATABASE_ADMIN_USER}")
IMAGE_NAME="database_server"

run_or_exit()
{
    "$@"
    local RESULT=$?
    if [[ "${RESULT}" -ne 0 ]]; then
        exit 1
    fi
}

#echo "Running as $(whoami)."

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
--user ${DATABASE_ADMIN_UID}:${DATABASE_ADMIN_GID} \
-e POSTGRES_USER="${DATABASE_ADMIN_USER}" \
-e POSTGRES_PASSWORD="${DATABASE_ADMIN_ROLE_PASSWORD}" \
-e PGDATA="/var/lib/postgresql/${POSTGRESQL_MAJOR_VERSION}/docker" \
-e CA_CRT="${CA_CRT}" \
-e POSTGRESQL_MAJOR_VERSION="${POSTGRESQL_MAJOR_VERSION}" \
-e DATABASE_ADMIN_USER="${DATABASE_ADMIN_USER}" \
-e DATABASE_SERVER_KEY_WITHOUT_PASSWORD="${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" \
-e DATABASE_SERVER_CRT="${DATABASE_SERVER_CRT}" \
-e DATABASE_ADMIN_KEY="${DATABASE_ADMIN_KEY}" \
-e DATABASE_ADMIN_KEY_PASSWORD="${DATABASE_ADMIN_KEY_PASSWORD}" \
-e DATABASE_ADMIN_CRT="${DATABASE_ADMIN_CRT}" \
-e DATABASE_NAME="${DATABASE_NAME}" \
-e APP_ROLE="${APP_ROLE}" \
-e APP_ROLE_PASSWORD="${APP_ROLE_PASSWORD}" \
--mount "type=bind,src=${DATABASE_ADMIN_HOME},dst=/var/lib/postgresql" \
-p 127.0.0.1:5433:5432/tcp \
-p "[::1]:5433:5432/tcp" \
--rm \
"${IMAGE_NAME}"

#echo "Finished running as $(whoami)."

#EOF

echo


#TODO test connection helper scripts