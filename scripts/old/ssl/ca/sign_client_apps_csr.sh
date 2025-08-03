#!/bin/bash


sudo \
env \
CLIENT_APP_CERTS_DIRECTORY="${CLIENT_APP_CERTS_DIRECTORY}" \
CLIENT_APP_CSR="${CLIENT_APP_CSR}" \
CA_ADMIN_HOME="${CA_ADMIN_HOME}" \
bash << "EOF"

if [ -f "${CA_ADMIN_HOME}/${CLIENT_APP_CSR}" ]; then
    # Remove old client app's signing request.
    echo "Removing old ${CA_ADMIN_HOME}/${CLIENT_APP_CSR}"
    rm ${CA_ADMIN_HOME}/${CLIENT_APP_CSR}
fi

if [ -f "${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CSR}" ]; then
    # Copy client app's signing request.
    echo "Copying ${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CSR}" \
         "to ${CA_ADMIN_HOME}/${CLIENT_APP_CSR}"
    cp -p \
    "${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CSR}" \
    "${CA_ADMIN_HOME}/${CLIENT_APP_CSR}"
else
    echo \
    "${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CSR} does not exist!" >&2
    exit 1
fi

EOF

if [ $? -ne 0 ]; then
    exit 1
fi


# Run as ca_admin user.
sudo -u "${CA_ADMIN}" \
env \
CLIENT_APP_CSR="${CLIENT_APP_CSR}" \
CLIENT_APP_CRT="${CLIENT_APP_CRT}" \
CA_KEY="${CA_KEY}" \
CA_KEY_PASSWORD="${CA_KEY_PASSWORD}" \
CA_CRT="${CA_CRT}" \
bash << "EOF"

echo "Running as $(whoami) user."
echo "Changing directory to ${HOME}"
cd "${HOME}"

if [ -f "${CLIENT_APP_CRT}" ]; then
    # Remove old client app's certificate.
    echo "Removing old ${HOME}/${CLIENT_APP_CRT}."
    rm ${HOME}/${CLIENT_APP_CRT}
fi

# Sign client app's signing request.
echo "Generating ${HOME}/${CLIENT_APP_CRT}."

openssl x509 \
-req \
-in "${CLIENT_APP_CSR}" \
-sha256 \
-CAkey "${CA_KEY}" \
-passin env:CA_KEY_PASSWORD \
-CA "${CA_CRT}" \
-CAcreateserial \
-out "${CLIENT_APP_CRT}" \
-days 365

echo "Changing mode of ${HOME}/${CLIENT_APP_CRT} to 644."
chmod 644 ${CLIENT_APP_CRT}

echo "Finished running as $(whoami) user."

EOF


sudo \
env \
CLIENT_APP_CERTS_DIRECTORY="${CLIENT_APP_CERTS_DIRECTORY}" \
CLIENT_APP_CSR="${CLIENT_APP_CSR}" \
CLIENT_APP_CRT="${CLIENT_APP_CRT}" \
CA_ADMIN_HOME="${CA_ADMIN_HOME}" \
CA_CRT="${CA_CRT}" \
SYSTEM_CERTIFICATES_DIRECTORY="${SYSTEM_CERTIFICATES_DIRECTORY}" \
bash << "EOF"

# Verify.
echo "Verifying ${CA_ADMIN_HOME}/${CLIENT_APP_CRT}:"
openssl verify \
-CAfile "${SYSTEM_CERTIFICATES_DIRECTORY}/ca-certificates.crt" \
"${CA_ADMIN_HOME}/${CLIENT_APP_CRT}"

# Remove client app's signing request copy.
echo "Removing ${CA_ADMIN_HOME}/${CLIENT_APP_CSR}."
rm "${CA_ADMIN_HOME}/${CLIENT_APP_CSR}"

if [ -f "${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CRT}" ]; then
    # Remove old client app's certificate.
    echo "Removing old ${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CRT}."
    rm "${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CRT}"
fi

# Move client app's certificate.
echo "Moving ${CA_ADMIN_HOME}/${CLIENT_APP_CRT}" \
     "to ${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CRT}"
mv \
"${CA_ADMIN_HOME}/${CLIENT_APP_CRT}" \
"${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CRT}"

if [ -f "${CLIENT_APP_CERTS_DIRECTORY}/${CA_CRT}" ]; then
    # Remove old local root ca certificate.
    echo "Removing old ${CLIENT_APP_CERTS_DIRECTORY}/${CA_CRT}"
    rm "${CLIENT_APP_CERTS_DIRECTORY}/${CA_CRT}"
fi

# Copy local root ca certificate.
echo "Copying ${CA_ADMIN_HOME}/${CA_CRT}" \
     "to ${CLIENT_APP_CERTS_DIRECTORY}/${CA_CRT}"
cp -p \
"${CA_ADMIN_HOME}/${CA_CRT}" \
"${CLIENT_APP_CERTS_DIRECTORY}/${CA_CRT}"

EOF