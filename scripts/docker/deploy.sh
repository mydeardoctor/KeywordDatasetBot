#!/bin/bash

cd ../common
set -a && source .env && set +a

cd ../docker
set -a && source .env && set +a

export DATABASE_ADMIN_UID=$(id -u "${DATABASE_ADMIN_USER}")
export DATABASE_ADMIN_GID=$(id -g "${DATABASE_ADMIN_USER}")

export APP_USER_UID=$(id -u)
export APP_USER_GID=$(id -g)

run_or_exit()
{
    "$@"
    local RESULT=$?
    if [[ "${RESULT}" -ne 0 ]]; then
        exit 1
    fi
}

cd ../common/ca
run_or_exit bash ./create_ca_admin_home_directory.sh

cd ../../docker/database
run_or_exit bash ./create_database_admin.sh

cd ../../common/app
run_or_exit bash ./create_app_directories.sh

cd ../../docker/ca
run_or_exit bash ./docker_install_openssl_generate_crts.sh

cd ..
run_or_exit bash ./generate_compose_yaml.sh
run_or_exit bash ./docker_compose_up.sh