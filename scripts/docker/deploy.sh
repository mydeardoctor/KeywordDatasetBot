#TODO может не засовывать докер команды в отдкльный скрипт?

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

cd ../../docker/ca
run_or_exit bash ./docker_install_openssl_generate_ca_crt.sh