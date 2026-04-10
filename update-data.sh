#!/bin/bash -e

if [ "$1" != "" ]; then
    ARGS=" -Dupdate=$1"
fi

mvn -B clean install exec:java -Pgithub $ARGS