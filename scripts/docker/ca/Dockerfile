FROM ubuntu:noble

WORKDIR /home

RUN <<EOF
apt-get update
apt-get -y install openssl
apt-get clean
rm -rf /var/lib/apt/lists/*
openssl version
EOF

COPY ./generate_ca_crt.sh .

ARG CA_ADMIN_HOME
RUN <<EOF
if [ -z "${CA_ADMIN_HOME}" ]; then \
    echo "Error! \"CA_ADMIN_HOME\" argument is not provided!"
    exit 1
fi
EOF
VOLUME "${CA_ADMIN_HOME}"

ENTRYPOINT ["bash", "./generate_ca_crt.sh"]