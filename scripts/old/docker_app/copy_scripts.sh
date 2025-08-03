#!/bin/bash

echo $(whoami)

if [ ! -d "${CLIENT_APP_CERTS_DIRECTORY}" ]; then
    echo "Creating ${CLIENT_APP_CERTS_DIRECTORY}"
    mkdir -p ${CLIENT_APP_CERTS_DIRECTORY}
fi

cp /certs_host/* ${CLIENT_APP_CERTS_DIRECTORY}/
chown ${CLIENT_APP_USER}:${CLIENT_APP_USER} ${CLIENT_APP_CERTS_DIRECTORY}/
chown ${CLIENT_APP_USER}:${CLIENT_APP_USER} ${CLIENT_APP_CERTS_DIRECTORY}/*
chmod 600 ${CLIENT_APP_CERTS_DIRECTORY}/*.key
chmod 600 ${CLIENT_APP_CERTS_DIRECTORY}/*.derkey
chmod 644 ${CLIENT_APP_CERTS_DIRECTORY}/*.crt
chmod 644 ${CLIENT_APP_CERTS_DIRECTORY}/*.csr

exec gosu ${CLIENT_APP_USER} java -jar /app/KeywordDatasetBot/KeywordDatasetBot_v0.0.1_afd30f7e.jar