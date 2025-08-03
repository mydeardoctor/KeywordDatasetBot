#!/bin/bash

#chown postgres:postgres /certs
#chown postgres:postgres /certs/database_server.key
#chown postgres:postgres /certs/database_server_without_password.key
#chown postgres:postgres /certs/database_server.csr
#chown postgres:postgres /certs/database_server.crt
#chown postgres:postgres /certs/local_root_ca.crt

#chmod 600 /certs
#chmod 600 /certs/database_server.key
#chmod 600 /certs/database_server_without_password.key
#chmod 600 /certs/database_server.csr
#chmod 600 /certs/database_server.crt
#chmod 600 /certs/local_root_ca.crt

echo $(whoami)

if [ ! -d "/certs" ]; then
    echo "Creating /certs"
    mkdir "/certs/"
fi

cp /certs_host/* /certs/
chown postgres:postgres /certs/
chown postgres:postgres /certs/*
chmod 600 /certs/*.key
chmod 644 /certs/*.crt

exec docker-entrypoint.sh postgres