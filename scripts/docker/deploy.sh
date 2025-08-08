#!/bin/bash

cd ../common
set -a && source .env && set +a

cd ../docker
set -a && source .env && set +a

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