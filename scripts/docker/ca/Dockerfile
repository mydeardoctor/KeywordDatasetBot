# syntax=docker/dockerfile:1

FROM ubuntu:noble

SHELL ["bash", "-c"]

WORKDIR /home

RUN <<EOF
apt-get update
apt-get -y install openssl
apt-get clean
rm -rf /var/lib/apt/lists/*
openssl version
EOF

COPY \
./generate_crts.sh \
./generate_ca_crt.sh \
./generate_database_server_csr.sh \
./generate_database_admin_csr.sh \
./generate_app_csr.sh \
./sign_database_servers_csr.sh \
./sign_database_admins_csr.sh \
./sign_app_csr.sh \
./

ARG CA_ADMIN_HOME
ARG DATABASE_CERTS_DIRECTORY
ARG APP_CERTS_DIRECTORY

RUN <<EOF
if [[ ( -z "${CA_ADMIN_HOME}" ) || \
      ( -z "${DATABASE_CERTS_DIRECTORY}" ) || \
      ( -z "${APP_CERTS_DIRECTORY}" ) ]]; then \
    echo "Error! Some arguments are not provided!" >&2
    exit 1
fi
EOF

VOLUME "${CA_ADMIN_HOME}"
VOLUME "${DATABASE_CERTS_DIRECTORY}"
VOLUME "${APP_CERTS_DIRECTORY}"

ENTRYPOINT ["bash", "./generate_crts.sh"]