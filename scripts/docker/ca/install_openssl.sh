#!/bin/bash

CA_IMAGE_NAME="ca"

sudo docker build \
-t "${CA_IMAGE_NAME}" \
-f ./Dockerfile \
--progress=plain \
.

# TODO Remove -it
sudo docker run \
--name container_ca \
-i \
-t \
--rm \
"${CA_IMAGE_NAME}"