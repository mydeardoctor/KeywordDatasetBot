#!/bin/bash


sudo \
env \
DATABASE_SERVER_DATA_DIRECTORY="${DATABASE_SERVER_DATA_DIRECTORY}" \
DATABASE_SERVER_CSR="${DATABASE_SERVER_CSR}" \
CA_ADMIN_HOME="${CA_ADMIN_HOME}" \
bash << "EOF"

if [ -f "${CA_ADMIN_HOME}/${DATABASE_SERVER_CSR}" ]; then
    # Remove old database server's signing request.
    echo "Removing old ${CA_ADMIN_HOME}/${DATABASE_SERVER_CSR}"
    rm ${CA_ADMIN_HOME}/${DATABASE_SERVER_CSR}
fi

if [ -f "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CSR}" ]; then
    # Copy database servers's signing request.
    echo "Copying ${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CSR}" \
         "to ${CA_ADMIN_HOME}/${DATABASE_SERVER_CSR}"
    cp -p \
    "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CSR}" \
    "${CA_ADMIN_HOME}/${DATABASE_SERVER_CSR}"
else
    echo "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CSR}" \
         "does not exist!" >&2
    exit 1
fi

EOF

if [ $? -ne 0 ]; then
    exit 1
fi


# Run as ca_admin user.
sudo -u "${CA_ADMIN}" \
env \
DATABASE_SERVER_CSR="${DATABASE_SERVER_CSR}" \
DATABASE_SERVER_CRT="${DATABASE_SERVER_CRT}" \
CA_KEY="${CA_KEY}" \
CA_KEY_PASSWORD="${CA_KEY_PASSWORD}" \
CA_CRT="${CA_CRT}" \
bash << "EOF"

echo "Running as $(whoami) user."
echo "Changing directory to ${HOME}"
cd "${HOME}"

if [ -f "${DATABASE_SERVER_CRT}" ]; then
    # Remove old database server's certificate.
    echo "Removing old ${HOME}/${DATABASE_SERVER_CRT}."
    rm ${HOME}/${DATABASE_SERVER_CRT}
fi

# Sign database server's signing request.
echo "Generating ${HOME}/${DATABASE_SERVER_CRT}."

openssl x509 \
-req \
-in "${DATABASE_SERVER_CSR}" \
-sha256 \
-CAkey "${CA_KEY}" \
-passin env:CA_KEY_PASSWORD \
-CA "${CA_CRT}" \
-CAcreateserial \
-out "${DATABASE_SERVER_CRT}" \
-days 365

echo "Changing mode of ${HOME}/${DATABASE_SERVER_CRT} to 644."
chmod 644 ${DATABASE_SERVER_CRT}

echo "Finished running as $(whoami) user."

EOF


sudo \
env \
DATABASE_SERVER_DATA_DIRECTORY="${DATABASE_SERVER_DATA_DIRECTORY}" \
DATABASE_SERVER_CSR="${DATABASE_SERVER_CSR}" \
DATABASE_SERVER_CRT="${DATABASE_SERVER_CRT}" \
CA_ADMIN_HOME="${CA_ADMIN_HOME}" \
CA_CRT="${CA_CRT}" \
SYSTEM_CERTIFICATES_DIRECTORY="${SYSTEM_CERTIFICATES_DIRECTORY}" \
bash << "EOF"

# Verify.
echo "Verifying ${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}:"
openssl verify \
-CAfile "${SYSTEM_CERTIFICATES_DIRECTORY}/ca-certificates.crt" \
"${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}"

# Remove database server's signing request copy.
echo "Removing ${CA_ADMIN_HOME}/${DATABASE_SERVER_CSR}."
rm "${CA_ADMIN_HOME}/${DATABASE_SERVER_CSR}"

if [ -f "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT}" ]; then
    # Remove old database server's certificate.
    echo \
    "Removing old ${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT}."
    rm "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT}"
fi

# Move database server's certificate.
echo "Moving ${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}" \
     "to ${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT}"
mv \
"${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}" \
"${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT}"

if [ -f "${DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT}" ]; then
    # Remove old local root ca certificate.
    echo "Removing old ${DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT}"
    rm "${DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT}"
fi

# Copy local root ca certificate.
echo "Copying ${CA_ADMIN_HOME}/${CA_CRT}" \
     "to ${DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT}"
cp -p \
"${CA_ADMIN_HOME}/${CA_CRT}" \
"${DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT}"

EOF