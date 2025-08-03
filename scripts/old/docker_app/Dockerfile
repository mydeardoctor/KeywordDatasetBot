# TODO is it noble
# Run as root
FROM ubuntu:noble

ARG CLIENT_APP_USER_HOME

WORKDIR ${CLIENT_APP_USER_HOME}

RUN apt-get update && apt-get install -y openjdk-21-jre unzip gosu

# TODO remove unzip from build?

COPY "./add_client_app_user.sh" "./add_client_app_user.sh"

# TODO how to get git hash
COPY "./KeywordDatasetBot_v0.0.1_afd30f7e.zip" "./KeywordDatasetBot_v0.0.1_afd30f7e.zip"

RUN unzip KeywordDatasetBot_v0.0.1_afd30f7e.zip

RUN rm KeywordDatasetBot_v0.0.1_afd30f7e.zip

ENTRYPOINT ["bash", "./add_client_app_user.sh"]