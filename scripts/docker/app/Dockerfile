# syntax=docker/dockerfile:1

FROM ubuntu:noble

SHELL ["bash", "-c"]

WORKDIR /home

RUN <<EOF
apt-get update
apt-get -y install git openjdk-21-jdk maven unzip
apt-get clean
rm -rf /var/lib/apt/lists/*
git --version
java --version
mvn --version
EOF

COPY \
./.git \
./.git

COPY \
./src \
./src/

COPY \
./scripts/docker/app/unzip.sh \
./archive_assembly_descriptor.xml \
./pom.xml \
./

RUN mvn clean verify

RUN bash ./unzip.sh

#VOLUME "${CA_ADMIN_HOME}"
#VOLUME "${DATABASE_CERTS_DIRECTORY}"
#VOLUME "${APP_CERTS_DIRECTORY}"

ENTRYPOINT ["bash"]