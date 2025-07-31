# TODO is it noble
FROM ubuntu:noble

WORKDIR /app

RUN apt-get update && apt-get install -y openjdk-21-jre iputils-ping telnet netcat-openbsd postgresql-client unzip gosu

ARG CLIENT_APP_USER
ARG CLIENT_APP_USER_HOME
ARG CLIENT_APP_AUDIO_DIRECTORY

RUN <<EOF
if ! id ${CLIENT_APP_USER} 1>/dev/null 2>&1; then
    echo "Creating ${CLIENT_APP_USER} user."

    adduser \
    --system \
    --comment "\"Keyword dataset bot\" client application" \
    --group \
    --home "${CLIENT_APP_USER_HOME}" \
    --shell /bin/bash \
    "${CLIENT_APP_USER}"
else
    echo "${CLIENT_APP_USER} user already exists, skipping."
fi
EOF

RUN <<EOF
# Change client app's HOME directory permission.
echo "Changing mode of ${CLIENT_APP_USER_HOME} to 700."
chmod 700 "${CLIENT_APP_USER_HOME}"
EOF

RUN <<EOF
if [ ! -d "${CLIENT_APP_AUDIO_DIRECTORY}" ]; then
    # Create client app's audio directory.
    echo "Creating ${CLIENT_APP_AUDIO_DIRECTORY}"
    mkdir "${CLIENT_APP_AUDIO_DIRECTORY}"
fi

# Change client app's audio directory permission.
echo "Changing mode of ${CLIENT_APP_AUDIO_DIRECTORY} to 700."
chmod 700 "${CLIENT_APP_AUDIO_DIRECTORY}"
EOF

# TODO how to get git hash FFS
COPY "./KeywordDatasetBot_v0.0.1_afd30f7e.zip" "./KeywordDatasetBot_v0.0.1_afd30f7e.zip"
COPY ./copy_scripts.sh ./copy_scripts.sh

RUN unzip KeywordDatasetBot_v0.0.1_afd30f7e.zip

# ENTRYPOINT ["java", "-jar"]
# CMD ["/app/KeywordDatasetBot/KeywordDatasetBot_v0.0.1_afd30f7e.jar"]
ENTRYPOINT ["bash", "/app/copy_scripts.sh"]