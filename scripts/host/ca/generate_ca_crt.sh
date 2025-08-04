#!/bin/bash

CA_KEY="ca.key"
CA_KEY_PERMISSIONS="600"
CA_CSR="ca.csr"
CA_CSR_PERMISSIONS="600"
CA_CRT="ca.crt"
OPENSSL_INSTALLATION_DIRECTORY="$(openssl version -d | cut -d '"' -f 2)"
OPENSSL_CNF="${OPENSSL_INSTALLATION_DIRECTORY}/openssl.cnf"
USER_CERTIFICATES_DIRECTORY="/usr/local/share/ca-certificates"

sudo \
env \
CA_ADMIN_USER="${CA_ADMIN_USER}" \
CA_ADMIN_GROUP="${CA_ADMIN_GROUP}" \
CA_ADMIN_HOME="${CA_ADMIN_HOME}" \
CA_KEY="${CA_KEY}" \
CA_KEY_PASSWORD="${CA_KEY_PASSWORD}" \
CA_KEY_PERMISSIONS="${CA_KEY_PERMISSIONS}" \
CA_CSR="${CA_CSR}" \
CA_CSR_PERMISSIONS="${CA_CSR_PERMISSIONS}" \
CA_CRT="${CA_CRT}" \
bash << "EOF"

echo "Running as $(whoami)."
echo "Changing directory to ${CA_ADMIN_HOME}"
cd "${CA_ADMIN_HOME}"

#if [ ! -f "${CA_KEY}" ]; then
#    echo "Generating ${CA_ADMIN_HOME}/${CA_KEY}" \
#         "with ${CA_ADMIN_USER}:${CA_ADMIN_GROUP} ownership."
#    openssl genpkey \
#    -algorithm RSA \
#    -AES-256-CBC \
#    -pass env:CA_KEY_PASSWORD \
#    -out "${CA_KEY}" \
#    -quiet
#
#else
#    echo "${CA_ADMIN_HOME}/${CA_KEY} already exists, skipping."
#
#    echo "Checking ownership of ${CA_ADMIN_HOME}/${CA_KEY}"
#    USER=$(stat -c "%U" "${CA_KEY}")
#    GROUP=$(stat -c "%G" "${CA_KEY}")
#    if [ "${USER}" != "${CA_ADMIN_USER}" ] || \
#       [ "${GROUP}" != "${CA_ADMIN_GROUP}" ]; then
#        echo "Changing ownership of ${CA_ADMIN_HOME}/${CA_KEY}" \
#             "to ${CA_ADMIN_USER}:${CA_ADMIN_GROUP}"
#        chown "${CA_ADMIN_USER}:${CA_ADMIN_GROUP}" "${CA_KEY}"
#    else
#        echo "Ownership of ${CA_ADMIN_HOME}/${CA_KEY}" \
#             "is already ${CA_ADMIN_USER}:${CA_ADMIN_GROUP}, skipping."
#    fi
#fi
#
#echo "Checking permissions of ${CA_ADMIN_HOME}/${CA_KEY}"
#PERMISSIONS=$(stat -c "%a" "${CA_KEY}")
#if [ "${PERMISSIONS}" != "${CA_KEY_PERMISSIONS}" ]; then
#    echo "Changing permissions of ${CA_ADMIN_HOME}/${CA_KEY}" \
#         "to ${CA_KEY_PERMISSIONS}"
#    chmod "${CA_KEY_PERMISSIONS}" "${CA_KEY}"
#else
#    echo "Permissions of ${CA_ADMIN_HOME}/${CA_KEY}" \
#         "are already ${CA_KEY_PERMISSIONS}, skipping."
#fi

if [ ! -f "${CA_CSR}" ] && [ ! -f "${CA_CRT}" ]; then
    echo "Generating ${CA_ADMIN_HOME}/${CA_CSR}" \
         "with ${CA_ADMIN_USER}:${CA_ADMIN_GROUP} ownership."
    openssl req \
    -new \
    -key "${CA_KEY}" \
    -passin env:CA_KEY_PASSWORD \
    -out "${CA_CSR}" \
    -subj "/O=my_dear_doctor/OU=ca/CN=ca_admin"

else
    echo "No need to generate ${CA_ADMIN_HOME}/${CA_CSR}, skipping."

    if [ -f "${CA_CSR}" ]; then
        echo "Checking ownership of ${CA_ADMIN_HOME}/${CA_CSR}"
        USER=$(stat -c "%U" "${CA_CSR}")
        GROUP=$(stat -c "%G" "${CA_CSR}")
        if [ "${USER}" != "${CA_ADMIN_USER}" ] || \
           [ "${GROUP}" != "${CA_ADMIN_GROUP}" ]; then
            echo "Changing ownership of ${CA_ADMIN_HOME}/${CA_CSR}" \
                 "to ${CA_ADMIN_USER}:${CA_ADMIN_GROUP}"
            chown "${CA_ADMIN_USER}:${CA_ADMIN_GROUP}" "${CA_CSR}"
        else
            echo "Ownership of ${CA_ADMIN_HOME}/${CA_CSR}" \
                 "is already ${CA_ADMIN_USER}:${CA_ADMIN_GROUP}, skipping."
        fi
    fi
fi

if [ -f "${CA_CSR}" ]; then
    echo "Checking permissions of ${CA_ADMIN_HOME}/${CA_CSR}"
    PERMISSIONS=$(stat -c "%a" "${CA_CSR}")
    if [ "${PERMISSIONS}" != "${CA_CSR_PERMISSIONS}" ]; then
        echo "Changing permissions of ${CA_ADMIN_HOME}/${CA_CSR}" \
             "to ${CA_CSR_PERMISSIONS}"
        chmod "${CA_CSR_PERMISSIONS}" "${CA_CSR}"
    else
        echo "Permissions of ${CA_ADMIN_HOME}/${CA_CSR}" \
             "are already ${CA_CSR_PERMISSIONS}, skipping."
    fi
fi

ls -l "${CA_ADMIN_HOME}"

echo "Finished running as $(whoami)."

EOF







#
#if [ ! -f "${CA_CRT}" ]; then
#    # Generate and self-sign certificate.
#    echo "Generating ${HOME}/${CA_CRT}."
#
#    openssl x509 \
#    -req \
#    -in "${CA_CSR}" \
#    -sha256 \
#    -key "${CA_KEY}" \
#    -passin env:CA_KEY_PASSWORD \
#    -extfile "${OPENSSL_CNF}" \
#    -extensions v3_ca \
#    -out "${CA_CRT}" \
#    -days 365
#else
#    echo "${HOME}/${CA_CRT} already exists, skipping."
#fi
#
#echo "Changing mode of ${HOME}/${CA_CRT} to 644."
#chmod 644 "${CA_CRT}"
#
#
#if [ -f "${CA_CSR}" ]; then
#    # Remove signing request.
#    echo "Removing ${HOME}/${CA_CSR}."
#
#    rm "${CA_CSR}"
#fi

echo