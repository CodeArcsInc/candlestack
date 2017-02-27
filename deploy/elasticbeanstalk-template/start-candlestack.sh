#! /bin/sh

export JAVA_HOME=/opt/jdk

component=candlestack
host=$(curl -s ipv4.icanhazip.com)

OPTS="-Xmx512m -Dcomponent=$component -Dhost=$host"
CONFIG="/opt/candlestack/${ENVIRONMENT}-candlestack.ini"	

$JAVA_HOME/bin/java $OPTS -jar /opt/candlestack/candlestack-1.1.0.jar $CONFIG
