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

DATABASE_ADMIN_IPV4_PATTERN="^[[:space:]]*hostssl[[:space:]]*all[[:space:]]*${DATABASE_ADMIN_USER}[[:space:]]*127.0.0.1/32[[:space:]]*cert[[:space:]]*clientname[[:space:]]*=[[:space:]]*CN[[:space:]]*$"
DATABASE_ADMIN_IPV4_LINE="hostssl all ${DATABASE_ADMIN_USER} 127.0.0.1/32 cert clientname=CN"
DATABASE_ADMIN_IPV6_PATTERN="^[[:space:]]*hostssl[[:space:]]*all[[:space:]]*${DATABASE_ADMIN_USER}[[:space:]]*::1/128[[:space:]]*cert[[:space:]]*clientname[[:space:]]*=[[:space:]]*CN[[:space:]]*$"
DATABASE_ADMIN_IPV6_LINE="hostssl all ${DATABASE_ADMIN_USER} ::1/128 cert clientname=CN"
DATABASE_ADMIN_LOCALHOST_PATTERN="^[[:space:]]*hostssl[[:space:]]*all[[:space:]]*${DATABASE_ADMIN_USER}[[:space:]]*localhost[[:space:]]*cert[[:space:]]*clientname[[:space:]]*=[[:space:]]*CN[[:space:]]*$"
DATABASE_ADMIN_LOCALHOST_LINE="hostssl all ${DATABASE_ADMIN_USER} localhost cert clientname=CN"
DATABASE_ADMIN_BRIDGE_PATTERN="^[[:space:]]*hostssl[[:space:]]*all[[:space:]]*${DATABASE_ADMIN_USER}[[:space:]]*172.17.0.1/32[[:space:]]*cert[[:space:]]*clientname[[:space:]]*=[[:space:]]*CN[[:space:]]*$"
DATABASE_ADMIN_BRIDGE_LINE="hostssl all ${DATABASE_ADMIN_USER} 172.17.0.1/32 cert clientname=CN"

APP_IPV4_PATTERN="^[[:space:]]*hostssl[[:space:]]*${DATABASE_NAME}[[:space:]]*${APP_ROLE}[[:space:]]*127.0.0.1/32[[:space:]]*cert[[:space:]]*clientname[[:space:]]*=[[:space:]]*CN[[:space:]]*$"
APP_IPV4_LINE="hostssl ${DATABASE_NAME} ${APP_ROLE} 127.0.0.1/32 cert clientname=CN"
APP_IPV6_PATTERN="^[[:space:]]*hostssl[[:space:]]*${DATABASE_NAME}[[:space:]]*${APP_ROLE}[[:space:]]*::1/128[[:space:]]*cert[[:space:]]*clientname[[:space:]]*=[[:space:]]*CN[[:space:]]*$"
APP_IPV6_LINE="hostssl ${DATABASE_NAME} ${APP_ROLE} ::1/128 cert clientname=CN"
APP_LOCALHOST_PATTERN="^[[:space:]]*hostssl[[:space:]]*${DATABASE_NAME}[[:space:]]*${APP_ROLE}[[:space:]]*localhost[[:space:]]*cert[[:space:]]*clientname[[:space:]]*=[[:space:]]*CN[[:space:]]*$"
APP_LOCALHOST_LINE="hostssl ${DATABASE_NAME} ${APP_ROLE} localhost cert clientname=CN"
APP_BRIDGE_PATTERN="^[[:space:]]*hostssl[[:space:]]*${DATABASE_NAME}[[:space:]]*${APP_ROLE}[[:space:]]*172.17.0.1/32[[:space:]]*cert[[:space:]]*clientname[[:space:]]*=[[:space:]]*CN[[:space:]]*$"
APP_BRIDGE_LINE="hostssl ${DATABASE_NAME} ${APP_ROLE} 172.17.0.1/32 cert clientname=CN"

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
"${DATABASE_ADMIN_IPV4_PATTERN}" \
"${DATABASE_ADMIN_IPV4_LINE}" \
"${POSTGRESQL_HBA_FILE}"

append \
"${DATABASE_ADMIN_IPV6_PATTERN}" \
"${DATABASE_ADMIN_IPV6_LINE}" \
"${POSTGRESQL_HBA_FILE}"

append \
"${DATABASE_ADMIN_LOCALHOST_PATTERN}" \
"${DATABASE_ADMIN_LOCALHOST_LINE}" \
"${POSTGRESQL_HBA_FILE}"

append \
"${DATABASE_ADMIN_BRIDGE_PATTERN}" \
"${DATABASE_ADMIN_BRIDGE_LINE}" \
"${POSTGRESQL_HBA_FILE}"

append \
"${APP_IPV4_PATTERN}" \
"${APP_IPV4_LINE}" \
"${POSTGRESQL_HBA_FILE}"

append \
"${APP_IPV6_PATTERN}" \
"${APP_IPV6_LINE}" \
"${POSTGRESQL_HBA_FILE}"

append \
"${APP_LOCALHOST_PATTERN}" \
"${APP_LOCALHOST_LINE}" \
"${POSTGRESQL_HBA_FILE}"

append \
"${APP_BRIDGE_PATTERN}" \
"${APP_BRIDGE_LINE}" \
"${POSTGRESQL_HBA_FILE}"