#FROM ubuntu:noble
FROM postgres:16.9

#RUN #pg_conftool show all

COPY ./edit_postgresql_configuration.sh /docker-entrypoint-initdb.d/edit_postgresql_configuration.sh

#ENTRYPOINT ["bash"]