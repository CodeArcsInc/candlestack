#! /bin/sh

cfg="/opt/filebeat/${ENVIRONMENT}-filebeat.yml"

/opt/filebeat/filebeat -v -e -c $cfg
