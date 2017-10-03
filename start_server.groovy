#!/bin/bash

export JAVA_OPTS='-Xmx1g -Xms1g'
groovy --indy StartServer.groovy "$@"

