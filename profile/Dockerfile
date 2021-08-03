FROM maven:3.8-jdk-11
RUN export DEBIAN_FRONTEND=noninteractive && \
  apt-get update && \
  apt-get install -y net-tools && \
  rm -rf /var/lib/apt/lists/*
