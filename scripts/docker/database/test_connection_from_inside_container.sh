#!/bin/bash

if [[ ( -z "${CA_CRT}" ) || \
      ( -z "${DATABASE_ADMIN_USER}" ) || \
      ( -z "${DATABASE_SERVER_ALTERNATE_HOSTNAME}" ) || \
      ( -z "${DATABASE_ADMIN_KEY}" ) || \
      ( -z "${DATABASE_ADMIN_KEY_PASSWORD}" ) || \
      ( -z "${DATABASE_ADMIN_CRT}" ) || \
      ( -z "${DATABASE_NAME}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

DATABASE_ADMIN_HOME_INSIDE_DOCKER=\
"/var/lib/postgresql"
DATABASE_CERTS_DIRECTORY_INSIDE_DOCKER=\
"${DATABASE_ADMIN_HOME_INSIDE_DOCKER}/certs"

echo "Connecting as ${DATABASE_ADMIN_USER}"
sudo docker exec "container_${DATABASE_SERVER_ALTERNATE_HOSTNAME}" \
psql \
"postgresql://${DATABASE_ADMIN_USER}@localhost:5432/${DATABASE_NAME}?sslcertmode=require&sslmode=verify-full&sslkey=${DATABASE_CERTS_DIRECTORY_INSIDE_DOCKER}/${DATABASE_ADMIN_KEY}&sslpassword=${DATABASE_ADMIN_KEY_PASSWORD}&sslcert=${DATABASE_CERTS_DIRECTORY_INSIDE_DOCKER}/${DATABASE_ADMIN_CRT}&sslrootcert=${DATABASE_CERTS_DIRECTORY_INSIDE_DOCKER}/${CA_CRT}" \
-c "SELECT audio_class_id FROM audio_class;"

echo