# Reproduction package for a JSON schema patterns experiment (https://doi.org/10.5281/zenodo.7586341)
#
# Copyright 2023, Christoph Köhnen <koehne02@ads.uni-passau.de>
# SPDX-License-Identifier: MIT

FROM ubuntu:20.04

MAINTAINER Christoph Köhnen <christoph.koehnen@uni-passau.de>

ENV DEBIAN_FRONTEND noninteractive
ENV LANG="C.UTF-8"
ENV LC_ALL="C.UTF-8"

# Install packages for experiments
RUN apt-get update && apt-get install -y --no-install-recommends \
        ca-certificates \
        dos2unix \
        git \
		maven \
		openjdk-11-jdk \
        openjdk-11-jre

# Install dev packages
RUN apt-get install -y --no-install-recommends \
		nano \
		sudo

# Add user
RUN useradd -m -G sudo -s /bin/bash repro && echo "repro:repro" | chpasswd
RUN usermod -a -G staff repro
USER repro
WORKDIR /home/repro

# Fetch a corpus of 80k json schema files
RUN ( git clone --no-checkout https://github.com/sdbs-uni-p/json-schema-corpus.git \
    && cd json-schema-corpus \
    && git checkout 79f808b )
ADD --chown=repro:repro . /home/repro/json-schema-patterns/

RUN chmod +x json-schema-patterns/*.sh
RUN ( cd json-schema-patterns \
    && ./setup.sh )
ENTRYPOINT ["sh", "-c", "cd json-schema-patterns && ./run-experiment.sh ../json-schema-corpus/json_schema_corpus && cd .. && bash"]
