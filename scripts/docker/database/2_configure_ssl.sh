#!/bin/bash

if [[ ( -z "${CA_CRT}" ) || \
      ( -z "${POSTGRESQL_MAJOR_VERSION}" ) || \
      ( -z "${DATABASE_ADMIN_USER}" ) || \
      ( -z "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" ) || \
      ( -z "${DATABASE_SERVER_CRT}" ) || \
      ( -z "${DATABASE_NAME}" ) || \
      ( -z "${APP_ROLE}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

DATABASE_ADMIN_HOME_INSIDE_DOCKER=\
"/var/lib/postgresql"
DATABASE_DATA_DIRECTORY_INSIDE_DOCKER=\
"${DATABASE_ADMIN_HOME_INSIDE_DOCKER}/${POSTGRESQL_MAJOR_VERSION}/docker"
DATABASE_CERTS_DIRECTORY_INSIDE_DOCKER=\
"${DATABASE_ADMIN_HOME_INSIDE_DOCKER}/certs"

POSTGRESQL_CONF_FILE="${DATABASE_DATA_DIRECTORY_INSIDE_DOCKER}/postgresql.conf"
POSTGRESQL_HBA_FILE="${DATABASE_DATA_DIRECTORY_INSIDE_DOCKER}/pg_hba.conf"

DATABASE_ADMIN_PATTERN="^[[:space:]]*hostssl[[:space:]]*all[[:space:]]*${DATABASE_ADMIN_USER}[[:space:]]*all[[:space:]]*cert[[:space:]]*clientname[[:space:]]*=[[:space:]]*CN[[:space:]]*$"
DATABASE_ADMIN_LINE="hostssl all ${DATABASE_ADMIN_USER} all cert clientname=CN"

APP_PATTERN="^[[:space:]]*hostssl[[:space:]]*${DATABASE_NAME}[[:space:]]*${APP_ROLE}[[:space:]]*all[[:space:]]*cert[[:space:]]*clientname[[:space:]]*=[[:space:]]*CN[[:space:]]*$"
APP_LINE="hostssl ${DATABASE_NAME} ${APP_ROLE} all cert clientname=CN"

append()
{
    local PATTERN="$1"
    local LINE="$2"
    local FILE="$3"

    if ! grep -Eq "${PATTERN}" "${FILE}"; then
        echo "Appending \"${LINE}\" to ${FILE}"
        echo "${LINE}" >> "${FILE}"
    else
        echo "\"${LINE}\" already exists in ${FILE}, skipping."
    fi
}

if [[ ( ! -f "${POSTGRESQL_CONF_FILE}" ) || \
      ( ! -f "${POSTGRESQL_HBA_FILE}" ) ]]; then
    echo "Some of postgresql config files do not exist!" >&2
    exit 1
fi

echo "Editing ${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s|^[[:space:]]*#\?[[:space:]]*ssl[[:space:]]*=.*$|ssl = on|" \
"${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s|^[[:space:]]*#\?[[:space:]]*ssl_key_file[[:space:]]*=.*$|ssl_key_file = '${DATABASE_CERTS_DIRECTORY_INSIDE_DOCKER}/${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}'|" \
"${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s|^[[:space:]]*#\?[[:space:]]*ssl_cert_file[[:space:]]*=.*$|ssl_cert_file = '${DATABASE_CERTS_DIRECTORY_INSIDE_DOCKER}/${DATABASE_SERVER_CRT}'|" \
"${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s|^[[:space:]]*#\?[[:space:]]*ssl_ca_file[[:space:]]*=.*$|ssl_ca_file = '${DATABASE_CERTS_DIRECTORY_INSIDE_DOCKER}/${CA_CRT}'|" \
"${POSTGRESQL_CONF_FILE}"

echo "Editing ${POSTGRESQL_HBA_FILE}"

sed \
-i \
-E \
"/^[[:space:]]*(local|host|hostnossl|hostgssenc|hostnogssenc|include|include_if_exists|include_dir)([[:space:]]|$)/ s|^|#|" \
"${POSTGRESQL_HBA_FILE}"

append \
"${DATABASE_ADMIN_PATTERN}" \
"${DATABASE_ADMIN_LINE}" \
"${POSTGRESQL_HBA_FILE}"

append \
"${APP_PATTERN}" \
"${APP_LINE}" \
"${POSTGRESQL_HBA_FILE}"