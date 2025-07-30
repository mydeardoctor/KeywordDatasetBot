#!/bin/bash

POSTGRESQL_CONF_FILE="/var/lib/postgresql/data/postgresql.conf"

if [ ! -f "${POSTGRESQL_CONF_FILE}" ]; then
    echo "Creating ${POSTGRESQL_CONF_FILE}"
    touch "${POSTGRESQL_CONF_FILE}"
fi

# Edit postgresql.conf to use SSL.
echo "Editing ${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s/^[[:space:]]*#\?[[:space:]]*ssl[[:space:]]*=.*$/ssl = on/" \
"${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s|^[[:space:]]*#\?[[:space:]]*ssl_key_file[[:space:]]*=.*$|ssl_key_file = '/certs/${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}'|" \
"${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s|^[[:space:]]*#\?[[:space:]]*ssl_cert_file[[:space:]]*=.*$|ssl_cert_file = '/certs/${DATABASE_SERVER_CRT}'|" \
"${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s|^[[:space:]]*#\?[[:space:]]*ssl_ca_file[[:space:]]*=.*$|ssl_ca_file = '/certs/${CA_CRT}'|" \
"${POSTGRESQL_CONF_FILE}"


POSTGRESQL_HBA_FILE="/var/lib/postgresql/data/pg_hba.conf"

if [ ! -f "${POSTGRESQL_HBA_FILE}" ]; then
    echo "Creating ${POSTGRESQL_HBA_FILE}"
    touch "${POSTGRESQL_HBA_FILE}"
fi

# Edit pg_hba.conf to use SSL.
echo "Editing ${POSTGRESQL_HBA_FILE}"

SSL_CONFIGURATION_LINE=\
"hostssl ${DATABASE_NAME} ${CLIENT_APP_ROLE} localhost cert clientname=CN"
PATTERN="^[[:space:]]*hostssl[[:space:]]*${DATABASE_NAME}[[:space:]]*${CLIENT_APP_ROLE}[[:space:]]*localhost[[:space:]]*cert[[:space:]]*clientname[[:space:]]*=[[:space:]]*CN[[:space:]]*$"
if ! grep -Eq "${PATTERN}" "${POSTGRESQL_HBA_FILE}"; then
    echo "Appending \"${SSL_CONFIGURATION_LINE}\" to ${POSTGRESQL_HBA_FILE}"
    echo "${SSL_CONFIGURATION_LINE}" >> "${POSTGRESQL_HBA_FILE}"
else
    echo "\"${SSL_CONFIGURATION_LINE}\" line already exists" \
         "in ${POSTGRESQL_HBA_FILE}, skipping."
fi