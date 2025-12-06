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

if [ -d "/data/knowstac/" ]; then
  rm -r /data/knowstac/
fi

mkdir /data/knowstac/  
mkdir /data/knowstac/elasticsearch/
mkdir /data/knowstac/elasticsearch/data
chmod 777 /data/knowstac/elasticsearch/data

docker-compose up
