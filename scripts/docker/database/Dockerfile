# syntax=docker/dockerfile:1

ARG POSTGRESQL_MAJOR_VERSION=16
ARG POSTGRESQL_MINOR_VERSION=9

FROM postgres:${POSTGRESQL_MAJOR_VERSION}.${POSTGRESQL_MINOR_VERSION}

SHELL ["bash", "-c"]

WORKDIR /home

COPY \
./1_configure_ssl.sh \
./2_create_database_roles_and_schema.sh \
/docker-entrypoint-initdb.d/

COPY \
./create_database_roles_and_schema.sql \
./

VOLUME "/var/lib/postgresql"

EXPOSE 5432

#TODO убрать
ENTRYPOINT ["bash"]