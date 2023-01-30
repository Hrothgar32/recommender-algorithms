# syntax = docker/dockerfile:1.2
FROM docker.io/ubuntu:focal
RUN apt-get update && apt-get -y install --reinstall ca-certificates && update-ca-certificates
RUN apt-get update && apt-get -y install
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-11-jdk curl rlwrap libssl-dev build-essential zlib1g-dev  libncurses5-dev \
    libgdbm-dev libnss3-dev  libreadline-dev libffi-dev libbz2-dev  automake-1.15 git liblzma-dev wget git clinfo vim  \
    leiningen software-properties-common vim

# mkl . This might ask questions '??' Important ??
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y intel-mkl

RUN curl -O https://download.clojure.org/install/linux-install-1.11.1.1149.sh
RUN chmod +x linux-install-1.11.1.1149.sh
RUN ./linux-install-1.11.1.1149.sh

WORKDIR /
COPY . /

RUN clj -Sforce -T:build all

# COPY --from=build /target/recommender-algorithms-standalone.jar /recommender-algorithms/recommender-algorithms-standalone.jar

EXPOSE $PORT

ENTRYPOINT exec java $JAVA_OPTS -jar /target/recommender-algorithms-standalone.jar
