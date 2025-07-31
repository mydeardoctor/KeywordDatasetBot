sudo docker build \
-t image_client_app \
--build-arg CLIENT_APP_USER=${CLIENT_APP_USER} \
--build-arg CLIENT_APP_USER_HOME=${CLIENT_APP_USER_HOME} \
--build-arg CLIENT_APP_AUDIO_DIRECTORY=${CLIENT_APP_AUDIO_DIRECTORY} \
.

sudo docker run \
--name container_client_app_26 \
-it \
--network custom_network \
-v "./certs:/certs_host:ro" \
-e BOT_TOKEN=${BOT_TOKEN} \
-e VOICE_EXTENSION=${VOICE_EXTENSION} \
-e DATABASE_SERVER_URL=${DATABASE_SERVER_URL} \
-e CLIENT_APP_USER=${CLIENT_APP_USER} \
-e CLIENT_APP_ROLE=${CLIENT_APP_ROLE} \
-e CLIENT_APP_PASSWORD=${CLIENT_APP_PASSWORD} \
-e CLIENT_APP_AUDIO_DIRECTORY=${CLIENT_APP_AUDIO_DIRECTORY} \
-e CLIENT_APP_CERTS_DIRECTORY=${CLIENT_APP_CERTS_DIRECTORY} \
-e CLIENT_APP_DER_KEY=${CLIENT_APP_DER_KEY} \
-e CLIENT_APP_KEY_PASSWORD=${CLIENT_APP_KEY_PASSWORD} \
-e CLIENT_APP_CRT=${CLIENT_APP_CRT} \
-e CA_CRT=${CA_CRT} \
image_client_app

#psql "postgresql://${CLIENT_APP_ROLE}:${CLIENT_APP_PASSWORD}@container_database_server_4:5432/keyword_database?sslcertmode=require&sslmode=verify-full&sslkey=${CLIENT_APP_CERTS_DIRECTORY}/client_app.key&sslpassword=${CLIENT_APP_KEY_PASSWORD}&sslcert=${CLIENT_APP_CERTS_DIRECTORY}/${CLIENT_APP_CRT}&sslrootcert=${CLIENT_APP_CERTS_DIRECTORY}/${CA_CRT}"
#psql: error: connection to server at "container_database_server_4" (172.18.0.3), port 5432 failed: server certificate for "localhost" does not match host name "container_database_server_4"