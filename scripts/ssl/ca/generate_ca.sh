#!/bin/bash

# Generate local root CA (certificate authority).

OPENSSL_INSTALLATION_DIRECTORY="$(openssl version -d | cut -d '"' -f 2)"
OPENSSL_CNF="${OPENSSL_INSTALLATION_DIRECTORY}/openssl.cnf"
USER_CERTIFICATES_DIRECTORY="/usr/local/share/ca-certificates"


# Run as ca_admin user.
sudo -u "${CA_ADMIN}" \
env \
CA_KEY="${CA_KEY}" \
CA_KEY_PASSWORD="${CA_KEY_PASSWORD}" \
CA_CSR="${CA_CSR}" \
CA_CRT="${CA_CRT}" \
OPENSSL_CNF="${OPENSSL_CNF}" \
bash << "EOF"


echo "Running as $(whoami) user."
echo "Changing directory to ${HOME}"
cd "${HOME}"


if [ ! -f "${CA_KEY}" ]; then
    # Generate private key.
    echo "Generating ${HOME}/${CA_KEY}."

    openssl genpkey \
    -algorithm RSA \
    -aes256 \
    -pass env:CA_KEY_PASSWORD \
    -out "${CA_KEY}" \
    -quiet
else
    echo "${HOME}/${CA_KEY} already exists, skipping."
fi

echo "Changing mode of ${HOME}/${CA_KEY} to 600."
chmod 600 "${CA_KEY}"


if [ ! -f "${CA_CSR}" ] && [ ! -f "${CA_CRT}" ]; then
    # Generate signing request.
    echo "Generating ${HOME}/${CA_CSR}."

    openssl req \
    -new \
    -key "${CA_KEY}" \
    -passin env:CA_KEY_PASSWORD \
    -out "${CA_CSR}" \
    -subj "/O=my_dear_doctor/OU=ca/CN=local_root_ca"

    echo "Changing mode of ${HOME}/${CA_CSR} to 600."
    chmod 600 "${CA_CSR}"
else
    echo "No need to generate ${HOME}/$CA_CSR, skipping."
fi

if [ ! -f "${CA_CRT}" ]; then
    # Generate and self-sign certificate.
    echo "Generating ${HOME}/${CA_CRT}."

    openssl x509 \
    -req \
    -in "${CA_CSR}" \
    -sha256 \
    -key "${CA_KEY}" \
    -passin env:CA_KEY_PASSWORD \
    -extfile "${OPENSSL_CNF}" \
    -extensions v3_ca \
    -out "${CA_CRT}" \
    -days 365
else
    echo "${HOME}/${CA_CRT} already exists, skipping."
fi

echo "Changing mode of ${HOME}/${CA_CRT} to 644."
chmod 644 "${CA_CRT}"


if [ -f "${CA_CSR}" ]; then
    # Remove signing request.
    echo "Removing ${HOME}/${CA_CSR}."

    rm "${CA_CSR}"
fi


echo "Finished running as $(whoami) user."


EOF




# Update ca certificates.

if [ -f "${USER_CERTIFICATES_DIRECTORY}/${CA_CRT}" ]; then
    # Remove old certificate in /usr/local/share/ca-certificates
    echo "Removing old ${USER_CERTIFICATES_DIRECTORY}/${CA_CRT}"
    sudo rm "${USER_CERTIFICATES_DIRECTORY}/${CA_CRT}"
fi

# Copy certificate to /usr/local/share/ca-certificates
echo "Copying ${CA_ADMIN_HOME}/${CA_CRT}" \
     "to ${USER_CERTIFICATES_DIRECTORY}/${CA_CRT}"
sudo cp \
"${CA_ADMIN_HOME}/${CA_CRT}" \
"${USER_CERTIFICATES_DIRECTORY}/${CA_CRT}"

# Remove old symlink.
echo "Removing old symlinks."
sudo find "${SYSTEM_CERTIFICATES_DIRECTORY}" \
-type l \
-lname "*${CA_CRT}" \
-exec sudo rm {} \;

# Update /etc/ssl/certs and /etc/ssl/certs/ca-certificates.crt
sudo update-ca-certificates

# Verify.
echo "Verifying ${CA_ADMIN_HOME}/${CA_CRT}:"
sudo openssl verify \
-CAfile "${SYSTEM_CERTIFICATES_DIRECTORY}/ca-certificates.crt" \
"${CA_ADMIN_HOME}/${CA_CRT}"