# syntax=docker/dockerfile:1
# TODO
# These arguments can be redefined
# when calling "docker build" command with "--build-arg" options.
# TODO вынести в env var
ARG POSTGRESQL_VERSION=16.9
# ARG TENSORFLOW_DATASETS_VERSION=4.9.6

# Base image that has python, pip, tensorflow with GPU support
# and jupyter notebook server preinsalled.
FROM postgres:${POSTGRESQL_VERSION}


# ARG HOST_DATABASE_SERVER_DATA_DIRECTORY
# ARG DATABASE_SERVER_KEY_WITHOUT_PASSWORD
# ARG DATABASE_SERVER_CRT
# ARG CA_CRT
# ENV DATABASE_NAME="${DATABASE_NAME}"
# ENV CLIENT_APP_ROLE="${CLIENT_APP_ROLE}"

# TODO user?
# WHERE am i after this command?
WORKDIR /home

# Use bind mounts

# COPY ${HOST_DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_KEY_WITHOUT_PASSWORD} .
# COPY ${HOST_DATABASE_SERVER_DATA_DIRECTORY}/${DATABASE_SERVER_CRT} .
# COPY ${HOST_DATABASE_SERVER_DATA_DIRECTORY}/${CA_CRT} .
# RUN mkdir /app/downloads
# RUN mkdir /app/build

# Install tools and packages needed to train Keyword Spotting model.
# RUN apt-get update && \
# apt-get -y install ffmpeg && \
# apt-get clean

# RUN pip install \
# pydub \
# seaborn

# ARG TENSORFLOW_DATASETS_VERSION
# RUN pip install tensorflow-datasets==${TENSORFLOW_DATASETS_VERSION}

ARG DATABASE_SERVER_USER
ARG DATABASE_SERVER_DATA_DIRECTORY="/var/lib/postgresql/16/docker"
ARG DATABASE_SERVER_KEY_WITHOUT_PASSWORD
ARG DATABASE_SERVER_CRT
ARG CA_CRT
ARG DATABASE_NAME
ARG CLIENT_APP_ROLE

ENV DATABASE_SERVER_USER="${DATABASE_SERVER_USER}"
ENV DATABASE_SERVER_DATA_DIRECTORY="${DATABASE_SERVER_DATA_DIRECTORY}"
ENV DATABASE_SERVER_KEY_WITHOUT_PASSWORD="${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}"
ENV DATABASE_SERVER_CRT="${DATABASE_SERVER_CRT}"
ENV CA_CRT="${CA_CRT}"
ENV DATABASE_NAME="${DATABASE_NAME}"
ENV CLIENT_APP_ROLE="${CLIENT_APP_ROLE}"

# Everytime
# COPY ./copy_scripts.sh /home/copy_scripts.sh

# Once
COPY ./create_database_roles_and_schema.sql /home/create_database_roles_and_schema.sql
COPY ./create_database_roles_and_schema.sh /docker-entrypoint-initdb.d/create_database_roles_and_schema.sh
COPY ./edit_postgresql_configuration.sh /docker-entrypoint-initdb.d/edit_postgresql_configuration.sh

# RUN chmod +x /home/copy_scripts.sh
# RUN bash ./configure_postgresql.sh

EXPOSE 5432

# Start jupyter notebook server.
# ENTRYPOINT ["jupyter", "notebook"]
# ENTRYPOINT ["bash"]
# CMD ["--allow-root", "--ip=0.0.0.0", "--no-browser", "--port=8888"]