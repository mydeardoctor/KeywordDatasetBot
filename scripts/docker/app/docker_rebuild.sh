#!/bin/bash

#if [[ ( -z "${CA_ADMIN_USER}" ) ]]; then
#    echo "Error! Some of environment variables are not set!" >&2
#    exit 1
#fi

run_or_exit()
{
    "$@"
    local RESULT=$?
    if [[ "${RESULT}" -ne 0 ]]; then
        exit 1
    fi
}

run_or_exit sudo docker build \
-t "java_build" \
-f ./Dockerfile \
--progress=plain \
../../../

run_or_exit sudo docker run \
--name "container_java_build" \
-i \
-t \
--rm \
"java_build"

echo