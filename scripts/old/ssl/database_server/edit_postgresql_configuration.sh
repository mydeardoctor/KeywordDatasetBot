#!/bin/bash


# Run as postgres user.
sudo -u "${DATABASE_SERVER_USER}" \
env \
DATABASE_SERVER_DATA_DIRECTORY="${DATABASE_SERVER_DATA_DIRECTORY}" \
DATABASE_SERVER_KEY_WITHOUT_PASSWORD="${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" \
DATABASE_SERVER_CRT="${DATABASE_SERVER_CRT}" \
CA_CRT="${CA_CRT}" \
DATABASE_NAME="${DATABASE_NAME}" \
CLIENT_APP_ROLE="${CLIENT_APP_ROLE}" \
bash << "EOF"


echo "Running as $(whoami) user."
echo "Changing directory to ${HOME}"
cd "${HOME}"

POSTGRESQL_CONF_FILE=\
$(psql -U postgres -d postgres -t -c "SHOW config_file;" | xargs)

if [ ! -f "${POSTGRESQL_CONF_FILE}" ]; then
    echo "${POSTGRESQL_CONF_FILE} does not exist!" >&2
    exit 1
fi

# Edit postgresql.conf to use SSL.
echo "Editing ${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s/^[[:space:]]*#\?[[:space:]]*ssl[[:space:]]*=.*$/ssl = on/" \
"${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s|^[[:space:]]*#\?[[:space:]]*ssl_key_file[[:space:]]*=.*$|ssl_key_file = '${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}'|" \
"${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s|^[[:space:]]*#\?[[:space:]]*ssl_cert_file[[:space:]]*=.*$|ssl_cert_file = '${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT}'|" \
"${POSTGRESQL_CONF_FILE}"

sed \
-i \
"s|^[[:space:]]*#\?[[:space:]]*ssl_ca_file[[:space:]]*=.*$|ssl_ca_file = '${DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT}'|" \
"${POSTGRESQL_CONF_FILE}"


POSTGRESQL_HBA_FILE=\
$(psql -U postgres -d postgres -t -c "SHOW hba_file;" | xargs)

if [ ! -f "${POSTGRESQL_HBA_FILE}" ]; then
    echo "${POSTGRESQL_HBA_FILE} does not exist!" >&2
    exit 1
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

echo "Finished running as $(whoami) user."

EOF


# Restart postgresql server.
echo "Restarting postgresql server."
sudo systemctl restart postgresql