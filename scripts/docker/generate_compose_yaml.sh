#!/bin/bash

if [[ ( -z "${CA_CRT}" ) || \
      ( -z "${POSTGRESQL_MAJOR_VERSION}" ) || \
      ( -z "${POSTGRESQL_MINOR_VERSION}" ) || \
      ( -z "${DATABASE_ADMIN_USER}" ) || \
      ( -z "${DATABASE_ADMIN_HOME}" ) || \
      ( -z "${DATABASE_ADMIN_ROLE_PASSWORD}" ) || \
      ( -z "${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" ) || \
      ( -z "${DATABASE_SERVER_CRT}" ) || \
      ( -z "${DATABASE_SERVER_ALTERNATE_HOSTNAME}" ) || \
      ( -z "${DATABASE_ADMIN_KEY}" ) || \
      ( -z "${DATABASE_ADMIN_KEY_PASSWORD}" ) || \
      ( -z "${DATABASE_ADMIN_CRT}" ) || \
      ( -z "${DATABASE_NAME}" ) || \
      ( -z "${DATABASE_SERVER_PORT}" ) || \
      ( -z "${DATABASE_SERVER_URL}" ) || \
      ( -z "${APP_NAME}" ) || \
      ( -z "${APP_CERTS_DIRECTORY}" ) || \
      ( -z "${APP_AUDIO_DIRECTORY}" ) || \
      ( -z "${APP_DER_KEY}" ) || \
      ( -z "${APP_KEY_PASSWORD}" ) || \
      ( -z "${APP_CRT}" ) || \
      ( -z "${APP_ROLE}" ) || \
      ( -z "${APP_ROLE_PASSWORD}" ) || \
      ( -z "${ARTIFACT_ID}" ) || \
      ( -z "${BOT_TOKEN}" ) || \
      ( -z "${VOICE_EXTENSION}" ) ]]; then
    echo "Error! Some of environment variables are not set!" >&2
    exit 1
fi

DATABASE_ADMIN_UID=$(id -u "${DATABASE_ADMIN_USER}")
DATABASE_ADMIN_GID=$(id -g "${DATABASE_ADMIN_USER}")
APP_USER_UID=$(id -u)
APP_USER_GID=$(id -g)

echo "Changing directory to project root."
cd ../..

if [[ ! -f "compose.yaml" ]]; then
    echo "Generating compose.yaml"

cat > "compose.yaml" << EOF
services:
  ${DATABASE_SERVER_ALTERNATE_HOSTNAME}:
    build:
      context: ./scripts/docker/database
      dockerfile: ./Dockerfile
      args:
        POSTGRESQL_MAJOR_VERSION: ${POSTGRESQL_MAJOR_VERSION}
        POSTGRESQL_MINOR_VERSION: ${POSTGRESQL_MINOR_VERSION}
    container_name: "container_${DATABASE_SERVER_ALTERNATE_HOSTNAME}"
    user: ${DATABASE_ADMIN_UID}:${DATABASE_ADMIN_GID}
    environment:
      POSTGRES_USER: ${DATABASE_ADMIN_USER}
EOF

cat >> "compose.yaml" << 'EOF'
      POSTGRES_PASSWORD: ${DATABASE_ADMIN_ROLE_PASSWORD}
EOF

cat >> "compose.yaml" << EOF
      PGDATA: /var/lib/postgresql/${POSTGRESQL_MAJOR_VERSION}/docker
      CA_CRT: ${CA_CRT}
      POSTGRESQL_MAJOR_VERSION: ${POSTGRESQL_MAJOR_VERSION}
      DATABASE_ADMIN_USER: ${DATABASE_ADMIN_USER}
      DATABASE_SERVER_KEY_WITHOUT_PASSWORD: ${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}
      DATABASE_SERVER_CRT: ${DATABASE_SERVER_CRT}
      DATABASE_ADMIN_KEY: ${DATABASE_ADMIN_KEY}
EOF

cat >> "compose.yaml" << 'EOF'
      DATABASE_ADMIN_KEY_PASSWORD: ${DATABASE_ADMIN_KEY_PASSWORD}
EOF

cat >> "compose.yaml" << EOF
      DATABASE_ADMIN_CRT: ${DATABASE_ADMIN_CRT}
      DATABASE_NAME: ${DATABASE_NAME}
      APP_ROLE: ${APP_ROLE}
EOF

cat >> "compose.yaml" << 'EOF'
      APP_ROLE_PASSWORD: ${APP_ROLE_PASSWORD}
EOF

cat >> "compose.yaml" << EOF
    volumes:
      - ${DATABASE_ADMIN_HOME}:/var/lib/postgresql:rw
    ports:
      - "127.0.0.1:5433:${DATABASE_SERVER_PORT}/tcp"

  keyword_dataset_bot:
    build:
      context: ./
      dockerfile: ./scripts/docker/app/Dockerfile
      args:
        ARTIFACT_ID: ${ARTIFACT_ID}
        APP_CERTS_DIRECTORY: ${APP_CERTS_DIRECTORY}
        APP_AUDIO_DIRECTORY: ${APP_AUDIO_DIRECTORY}
    container_name: "container_${APP_NAME}"
    user: ${APP_USER_UID}:${APP_USER_GID}
    environment:
      ARTIFACT_ID: ${ARTIFACT_ID}
EOF

cat >> "compose.yaml" << 'EOF'
      BOT_TOKEN: ${BOT_TOKEN}
EOF

cat >> "compose.yaml" << EOF
      VOICE_EXTENSION: ${VOICE_EXTENSION}
EOF

cat >> "compose.yaml" << 'EOF'
      DATABASE_SERVER_URL: ${DATABASE_SERVER_URL}
EOF

cat >> "compose.yaml" << EOF
      APP_ROLE: ${APP_ROLE}
EOF

cat >> "compose.yaml" << 'EOF'
      APP_ROLE_PASSWORD: ${APP_ROLE_PASSWORD}
EOF

cat >> "compose.yaml" << EOF
      APP_CERTS_DIRECTORY: ${APP_CERTS_DIRECTORY}
      APP_AUDIO_DIRECTORY: ${APP_AUDIO_DIRECTORY}
      APP_DER_KEY: ${APP_DER_KEY}
EOF

cat >> "compose.yaml" << 'EOF'
      APP_KEY_PASSWORD: ${APP_KEY_PASSWORD}
EOF

cat >> "compose.yaml" << EOF
      APP_CRT: ${APP_CRT}
      CA_CRT: ${CA_CRT}
    volumes:
      - ${APP_CERTS_DIRECTORY}:${APP_CERTS_DIRECTORY}:r
      - ${APP_AUDIO_DIRECTORY}:${APP_AUDIO_DIRECTORY}:rw
    ports:
      - "127.0.0.1:8080:8080/tcp"
    depends_on:
      - ${DATABASE_SERVER_ALTERNATE_HOSTNAME}
EOF

else
    echo "compose.yaml already exists, skipping."
fi

echo