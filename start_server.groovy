#!/bin/bash

export JAVA_OPTS='-Xmx1g -Xms1g'
java -jar build/libs/ignite-demo-0.1.0-all.jar "$@"
