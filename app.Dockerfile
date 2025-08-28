# syntax=docker/dockerfile:1

FROM ubuntu:noble AS rebuild_stage

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
./.git/

COPY \
./src \
./src/

COPY \
./archive_assembly_descriptor.xml \
./pom.xml \
./

COPY \
./scripts/common/app/unzip.sh \
./scripts/common/app/

RUN mvn clean verify

WORKDIR /home/scripts/common/app

RUN bash ./unzip.sh

FROM ubuntu:noble

SHELL ["bash", "-c"]

WORKDIR /home

RUN <<EOF
apt-get update
apt-get -y install openjdk-21-jre-headless
apt-get clean
rm -rf /var/lib/apt/lists/*
java --version
EOF

ARG ARTIFACT_ID
ARG APP_CERTS_DIRECTORY
ARG APP_AUDIO_DIRECTORY
RUN <<EOF
if [[ ( -z "${ARTIFACT_ID}" ) || \
      ( -z "${APP_CERTS_DIRECTORY}" ) || \
      ( -z "${APP_AUDIO_DIRECTORY}" ) ]]; then \
    echo "Error! Some arguments are not provided!" >&2
    exit 1
fi
EOF

COPY --from=rebuild_stage \
/home/target/${ARTIFACT_ID} \
./target/${ARTIFACT_ID}/

COPY \
./scripts/common/app/run.sh \
./scripts/common/app/

VOLUME "${APP_CERTS_DIRECTORY}"
VOLUME "${APP_AUDIO_DIRECTORY}"

WORKDIR /home/scripts/common/app

ENTRYPOINT ["bash", "./run.sh"]