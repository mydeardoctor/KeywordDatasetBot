sudo docker build \
-t image_sandbox \
--progress=plain \
.

sudo docker run \
--name container_sandbox \
-it \
--rm \
-e POSTGRES_USER="postgres" \
-e POSTGRES_PASSWORD="postgres" \
image_sandbox