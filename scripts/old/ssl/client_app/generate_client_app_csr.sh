#!/bin/bash

# Generate client app certificate signing request.

# Run as client app user.
sudo -u "${CLIENT_APP_USER}" \
env \
CLIENT_APP_CERTS_DIRECTORY="${CLIENT_APP_CERTS_DIRECTORY}" \
CLIENT_APP_KEY="${CLIENT_APP_KEY}" \
CLIENT_APP_KEY_PASSWORD="${CLIENT_APP_KEY_PASSWORD}" \
CLIENT_APP_DER_KEY="${CLIENT_APP_DER_KEY}" \
CLIENT_APP_CSR="${CLIENT_APP_CSR}" \
CLIENT_APP_ROLE="${CLIENT_APP_ROLE}" \
bash << "EOF"


echo "Running as $(whoami) user."

echo "Changing directory to ${HOME}"
cd "${HOME}"

if [ ! -d "${CLIENT_APP_CERTS_DIRECTORY}" ]; then
    # Create client app's certificate directory.
    echo "Creating ${CLIENT_APP_CERTS_DIRECTORY}"
    mkdir "${CLIENT_APP_CERTS_DIRECTORY}"
fi

# Change client app's certificate directory permission.
echo "Changing mode of ${CLIENT_APP_CERTS_DIRECTORY} to 700."
chmod 700 "${CLIENT_APP_CERTS_DIRECTORY}"

echo "Changing directory to ${CLIENT_APP_CERTS_DIRECTORY}"
cd "${CLIENT_APP_CERTS_DIRECTORY}"

if [ ! -f "${CLIENT_APP_KEY}" ]; then
    # Generate private key.
    echo "Generating ${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_KEY}."

    openssl genpkey \
    -algorithm RSA \
    -AES-256-CBC \
    -pass env:CLIENT_APP_KEY_PASSWORD \
    -out ${CLIENT_APP_KEY} \
    -quiet
else
    echo \
    "${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_KEY} already exists, skipping."
fi

echo "Changing mode of ${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_KEY} to 600."
chmod 600 "${CLIENT_APP_KEY}"

if [ -f "${CLIENT_APP_DER_KEY}" ]; then
    # Remove old private DER key.
    echo "Removing old ${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_DER_KEY}."
    rm "${CLIENT_APP_DER_KEY}"
fi

# Generate private DER key.
echo "Generating ${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_DER_KEY}."
openssl pkcs8 \
-topk8 \
-inform PEM \
-in "${CLIENT_APP_KEY}" \
-passin env:CLIENT_APP_KEY_PASSWORD \
-outform DER \
-out "${CLIENT_APP_DER_KEY}" \
-passout env:CLIENT_APP_KEY_PASSWORD

# Verify private DER key.
echo "Verifying ${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_DER_KEY}."
openssl pkcs8 \
-inform DER \
-in "${CLIENT_APP_DER_KEY}" \
-passin env:CLIENT_APP_KEY_PASSWORD \
-out /dev/null

echo \
"Changing mode of ${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_DER_KEY} to 600."
chmod 600 "${CLIENT_APP_DER_KEY}"

if [ ! -f "${CLIENT_APP_CSR}" ]; then
    # Generate signing request.
    echo "Generating ${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CSR}."

    # CN must match client's database role for ssl full verification.
    openssl req \
    -new \
    -key "${CLIENT_APP_KEY}" \
    -passin env:CLIENT_APP_KEY_PASSWORD \
    -out "${CLIENT_APP_CSR}" \
    -subj "/O=my_dear_doctor/OU=client_app/CN=${CLIENT_APP_ROLE}"
else
    echo \
    "${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CSR} already exists, skipping."
fi

echo "Changing mode of ${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CSR} to 644."
chmod 644 "${CLIENT_APP_CSR}"


echo "Finished running as $(whoami) user."


EOF