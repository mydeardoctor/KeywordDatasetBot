#$ docker run --name some-postgres -e  -d postgres


# TODO скрипт, который меняет conf и hba

#TODO DATABASE_SERVER_DATA_DIRECTORY должна узнаваться динамически!!!
sudo docker build \
-t image_database_server \
--build-arg DATABASE_SERVER_USER="${DATABASE_SERVER_USER}" \
--build-arg DATABASE_SERVER_KEY_WITHOUT_PASSWORD="${DATABASE_SERVER_KEY_WITHOUT_PASSWORD}" \
--build-arg DATABASE_SERVER_CRT="${DATABASE_SERVER_CRT}" \
--build-arg CA_CRT="${CA_CRT}" \
--build-arg DATABASE_NAME="${DATABASE_NAME}" \
--build-arg CLIENT_APP_ROLE="${CLIENT_APP_ROLE}" \
.
#--build-arg HOST_DATABASE_SERVER_DATA_DIRECTORY=${DATABASE_SERVER_DATA_DIRECTORY} \
#--build-arg DATABASE_SERVER_KEY_WITHOUT_PASSWORD=${DATABASE_SERVER_KEY_WITHOUT_PASSWORD} \
#--build-arg DATABASE_SERVER_CRT=${DATABASE_SERVER_CRT} \
#--build-arg CA_CRT=${CA_CRT} \


#Works:
#export POSTGRESQL_VERSION="16.9"
#docker run \
#--name container_database_server4 \
#-p 127.0.0.1:5433:5432/tcp \
#-p "[::1]:5433:5432/tcp" \
#-i \
#-t \
#-e POSTGRES_USER="postgres" \
#-e POSTGRES_PASSWORD="postgres" \
#postgres:"${POSTGRESQL_VERSION}"

sudo docker run \
--name container_database_server37 \
-p 127.0.0.1:5433:5432/tcp \
-p "[::1]:5433:5432/tcp" \
-it \
-v "./certs:/certs_host:ro" \
-e POSTGRES_USER="postgres" \
-e POSTGRES_PASSWORD="postgres" \
-e CLIENT_APP_ROLE="${CLIENT_APP_ROLE}" \
-e CLIENT_APP_PASSWORD="${CLIENT_APP_PASSWORD}" \
-e DATABASE_NAME="${DATABASE_NAME}" \
image_database_server


#initdb: warning: enabling "trust" authentication for local connections
#initdb: hint: You can change this by editing pg_hba.conf or using the option -A, or --auth-local and --auth-host, the next time you run initdb.
#Success. You can now start the database server using:
#
#    pg_ctl -D /var/lib/postgresql/data -l logfile start

#Works but prompts for password
#sudo -u postgres psql -U postgres -d postgres -h localhost -p 5433
