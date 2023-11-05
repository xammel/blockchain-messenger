FROM openjdk:8-jre-alpine

ARG SSH_PRIVATE_KEY

RUN apk add --no-cache git openssh && \
    mkdir /development

# GIT

RUN mkdir -p /root/.ssh/

RUN touch /root/.ssh/id_rsa

RUN chmod 600 /root/.ssh/id_rsa

RUN echo "$SSH_PRIVATE_KEY" > /root/.ssh/id_rsa

RUN echo "IdentityFile /root/.ssh/id_rsa" >> /etc/ssh/ssh_config && \
    echo -e "StrictHostKeyChecking no" >> /etc/ssh/ssh_config

RUN git clone git@github.com:xammel/scala-blockchain.git

# SBT
ARG SBT_VERSION
RUN apk add --no-cache bash

RUN apk add --no-cache --virtual=build-dependencies curl

RUN wget https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz

RUN tar -C /usr/local -xzvf sbt-$SBT_VERSION.tgz

RUN ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt

RUN chmod 0755 /usr/local/bin/sbt

RUN apk del build-dependencies

RUN cd /development && \
    sbt sbtVersion

COPY docker/init.sh /etc/init.sh

RUN chmod +x /etc/init.sh

ENTRYPOINT [ "/etc/init.sh" ]

EXPOSE 8080