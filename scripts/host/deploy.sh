#!/bin/bash

cd ../common
set -a && source .env && set +a

cd ../host
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

cd ../../host/ca
run_or_exit bash ./install_openssl.sh

run_or_exit bash ./generate_ca_crt.sh