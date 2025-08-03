sudo docker build \
-t image_sandbox \
.

sudo docker run \
--name container_sandbox \
-it \
--rm \
image_sandbox