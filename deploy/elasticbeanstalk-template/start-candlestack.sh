#! /bin/sh

export JAVA_HOME=/opt/jdk

COMPONENT=candlestack
HOST=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)

OPTS="-Xmx512m -Dcomponent=$COMPONENT -Dhost=$HOST"
CONFIG="/opt/candlestack/${ENVIRONMENT}-candlestack.ini"	

$JAVA_HOME/bin/java $OPTS -jar /opt/candlestack/candlestack-1.1.0.jar $CONFIG
