#!/bin/bash
#
#
#

#
# Arguments:
# $1 : Optional, if set to 'false' then we will not save the image to a file afterwords.
#

# Run this with sudo
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

set -e

# If tag is not set, then set it to 'latest' as a default value.
tag=${tag:-'latest'}

([ -d target ] && rm -rf target) || true
mkdir target
cp ../../../../know-stac-web/target/know-stac.war target/know-stac.war
cp -R ../../../../envcfg/prod target/appcfg

docker build -t terraframe/knowstac:$tag .

if [ "${CGR_RELEASE_VERSION:-'latest'}" != "latest" ]; then
  docker tag terraframe/knowstac:$tag terraframe/knowstac:latest
fi

if [ "$1 != 'false'" ]; then
  docker save terraframe/knowstac:$tag | gzip > target/knowstac.dimg.gz
fi
