#!/bin/sh

WORKDIR=$(pwd)
ROOTDIR="$WORKDIR/$(dirname "$0")/.."
WEBDIR="$ROOTDIR/webservice/web"

cd "$WEBDIR"
npm run build

cd "$ROOTDIR"
mvn package
