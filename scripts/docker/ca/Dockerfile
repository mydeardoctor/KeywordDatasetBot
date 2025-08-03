FROM ubuntu:noble

RUN apt-get update && \
    apt-get -y install openssl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    openssl version

ENTRYPOINT ["bash"]