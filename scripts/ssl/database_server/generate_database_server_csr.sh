#!/bin/bash

# Generate database server certificate signing request.

# Run as database server user.
sudo -u "${DATABASE_SERVER_USER}" \
env \
DATABASE_SERVER_DATA_DIRECTORY="${DATABASE_SERVER_DATA_DIRECTORY}" \
DATABASE_SERVER_KEY="${DATABASE_SERVER_KEY}" \
DATABASE_SERVER_KEY_PASSWORD="${DATABASE_SERVER_KEY_PASSWORD}" \
DATABASE_SERVER_KEY_WITHOUT_PASSWORD="${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" \
DATABASE_SERVER_CSR="${DATABASE_SERVER_CSR}" \
DATABASE_SERVER_HOSTNAME="${DATABASE_SERVER_HOSTNAME}" \
bash << "EOF"


echo "Running as $(whoami) user."
echo "Changing directory to ${HOME}"
cd "${HOME}"

if [ ! -d "${DATABASE_SERVER_DATA_DIRECTORY}" ]; then
    # Create database server's data directory.
    echo "Creating ${DATABASE_SERVER_DATA_DIRECTORY}"
    mkdir "${DATABASE_SERVER_DATA_DIRECTORY}"
fi

# Change database server's data directory permission.
echo "Changing mode of ${DATABASE_SERVER_DATA_DIRECTORY} to 700."
chmod 700 "${DATABASE_SERVER_DATA_DIRECTORY}"

echo "Changing directory to ${DATABASE_SERVER_DATA_DIRECTORY}"
cd "${DATABASE_SERVER_DATA_DIRECTORY}"

if [ ! -f "${DATABASE_SERVER_KEY}" ]; then
    # Generate private key.
    echo "Generating ${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_KEY}."

    openssl genpkey \
    -algorithm RSA \
    -aes256 \
    -pass env:DATABASE_SERVER_KEY_PASSWORD \
    -out "${DATABASE_SERVER_KEY}" \
    -quiet
else
    echo "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_KEY}" \
         "already exists, skipping."
fi

echo "Changing mode of" \
     "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_KEY} to 600."
chmod 600 "${DATABASE_SERVER_KEY}"

if [ -f "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" ]; then
    # Remove old private key without password.
    echo \
    "Removing old" \
    "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}"
    rm "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}"
fi

# Generate private key without password.
echo \
"Generating" \
"${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}"
openssl pkey \
-in "${DATABASE_SERVER_KEY}" \
-passin env:DATABASE_SERVER_KEY_PASSWORD \
-out "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}"

echo \
"Changing mode of" \
"${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" \
"to 600."
chmod 600 "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}"

if [ ! -f "${DATABASE_SERVER_CSR}" ]; then
    # Generate signing request.
    echo "Generating ${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CSR}."

    # CN must match database server's hostname for ssl full verification.
    openssl req \
    -new \
    -key "${DATABASE_SERVER_KEY}" \
    -passin env:DATABASE_SERVER_KEY_PASSWORD \
    -out "${DATABASE_SERVER_CSR}" \
    -subj "/O=my_dear_doctor/OU=database_server/CN=${DATABASE_SERVER_HOSTNAME}"
else
    echo "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CSR}" \
         "already exists, skipping."
fi

echo "Changing mode of" \
     "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CSR} to 644."
chmod 644 "${DATABASE_SERVER_CSR}"


echo "Finished running as $(whoami) user."


EOF