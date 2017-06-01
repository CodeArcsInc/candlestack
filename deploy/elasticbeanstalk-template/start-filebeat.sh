#! /bin/sh

CONFIG="/opt/filebeat/${ENVIRONMENT}-filebeat.yml"

/opt/filebeat/filebeat -c $CONFIG
