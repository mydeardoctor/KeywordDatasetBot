#!/bin/bash

if [[ ( -z "${CA_ADMIN_USER}" ) || \
      ( -z "${CA_ADMIN_GROUP}" ) || \
      ( -z "${CA_ADMIN_HOME}" ) || \
      ( -z "${POSTGRESQL_MAJOR_VERSION}" ) || \
      ( -z "${DATABASE_ADMIN_USER}" ) || \
      ( -z "${DATABASE_SERVER_CSR}" ) || \
      ( -z "${DATABASE_SERVER_CRT}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

sudo \
env \
CA_ADMIN_USER="${CA_ADMIN_USER}" \
CA_ADMIN_GROUP="${CA_ADMIN_GROUP}" \
CA_ADMIN_HOME="${CA_ADMIN_HOME}" \
POSTGRESQL_MAJOR_VERSION="${POSTGRESQL_MAJOR_VERSION}" \
DATABASE_ADMIN_USER="${DATABASE_ADMIN_USER}" \
DATABASE_SERVER_CSR="${DATABASE_SERVER_CSR}" \
DATABASE_SERVER_CRT="${DATABASE_SERVER_CRT}" \
bash << "EOF"

echo "Running as $(whoami)."
echo "Changing directory to ${CA_ADMIN_HOME}"
cd "${CA_ADMIN_HOME}"

DATABASE_ADMIN_HOME=$(getent passwd "${DATABASE_ADMIN_USER}" | cut -d : -f 6)
DATABASE_DATA_DIRECTORY=\
"${DATABASE_ADMIN_HOME}/${POSTGRESQL_MAJOR_VERSION}/main"

if [[ -f "${DATABASE_DATA_DIRECTORY}/${DATABASE_SERVER_CSR}" ]]; then
    echo "Moving ${DATABASE_DATA_DIRECTORY}/${DATABASE_SERVER_CSR}" \
         "to ${CA_ADMIN_HOME}"
    mv -f \
    "${DATABASE_DATA_DIRECTORY}/${DATABASE_SERVER_CSR}" \
    "${CA_ADMIN_HOME}"
else
    echo "${DATABASE_DATA_DIRECTORY}/${DATABASE_SERVER_CSR}" \
         "does not exist!" >&2
    exit 1
fi

if [[ ! -f "${DATABASE_SERVER_CRT}" ]]; then
    echo "Generating ${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}" \
         "with ${CA_ADMIN_USER}:${CA_ADMIN_GROUP} ownership."
    openssl x509 \
    -req \
    -in "${DATABASE_SERVER_CSR}" \
    -sha256 \
    -extfile conf \
    -CAkey "${CA_KEY}" \
    -passin env:CA_KEY_PASSWORD \
    -CA "${CA_CRT}" \
    -CAcreateserial \
    -out "${DATABASE_SERVER_CRT}" \
    -days 365
else

fi


#
#echo "Changing mode of ${HOME}/${DATABASE_SERVER_CRT} to 644."
#chmod 644 ${DATABASE_SERVER_CRT}
#
#echo "Finished running as $(whoami) user."
#
#EOF
#
#
#sudo \
#env \
#DATABASE_SERVER_DATA_DIRECTORY="${DATABASE_SERVER_DATA_DIRECTORY}" \
#DATABASE_SERVER_CSR="${DATABASE_SERVER_CSR}" \
#DATABASE_SERVER_CRT="${DATABASE_SERVER_CRT}" \
#CA_ADMIN_HOME="${CA_ADMIN_HOME}" \
#CA_CRT="${CA_CRT}" \
#SYSTEM_CERTIFICATES_DIRECTORY="${SYSTEM_CERTIFICATES_DIRECTORY}" \
#bash << "EOF"
#
## Verify.
#echo "Verifying ${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}:"
#openssl verify \
#-CAfile "${SYSTEM_CERTIFICATES_DIRECTORY}/ca-certificates.crt" \
#"${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}"
#
## Remove database server's signing request copy.
#echo "Removing ${CA_ADMIN_HOME}/${DATABASE_SERVER_CSR}."
#rm "${CA_ADMIN_HOME}/${DATABASE_SERVER_CSR}"
#
#if [ -f "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT}" ]; then
#    # Remove old database server's certificate.
#    echo \
#    "Removing old ${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT}."
#    rm "${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT}"
#fi
#
## Move database server's certificate.
#echo "Moving ${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}" \
#     "to ${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT}"
#mv \
#"${CA_ADMIN_HOME}/${DATABASE_SERVER_CRT}" \
#"${DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT}"
#
#if [ -f "${DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT}" ]; then
#    # Remove old local root ca certificate.
#    echo "Removing old ${DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT}"
#    rm "${DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT}"
#fi
#
## Copy local root ca certificate.
#echo "Copying ${CA_ADMIN_HOME}/${CA_CRT}" \
#     "to ${DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT}"
#cp -p \
#"${CA_ADMIN_HOME}/${CA_CRT}" \
#"${DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT}"

echo "Finished running as $(whoami)."

EOF

echo