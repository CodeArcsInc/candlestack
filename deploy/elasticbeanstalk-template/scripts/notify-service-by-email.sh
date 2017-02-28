#! /bin/bash 

NOTIFICATIONTYPE="$1"
HOSTNAME="$2"
HOSTADDRESS="$3"
HOSTGROUPNAME="$4"
SERVICEDESC="$5"
SERVICEOUTPUT="$6"
SERVICENOTES="$7"
LONGDATETIME="$8"
CONTACTEMAILS="$9"
NAGIOSURL="${10}"
LOGSURL="${11}"

SERVICEOUTPUT=$(echo "$SERVICEOUTPUT" | sed -e 's/br\//\n/g')

TEXTMSG=$(cat << EOF
Date/Time: 
$LONGDATETIME

Service:
$SERVICEDESC

Service Check Output:
$SERVICEOUTPUT

Host Group: 
$HOSTGROUPNAME

Host: 
$HOSTNAME ($HOSTADDRESS)

Service Check Notes:
$SERVICENOTES

Nagios URL:
$NAGIOSURL

Logs URL:
$LOGSURL
EOF
)

FROM="$TODO_FROM_EMAIL_ADDRESS"
if [ "$NOTIFICATIONTYPE" == "RECOVERY" ] 
then
else
fi

aws --region eu-west-1 ses send-email \
	--from "$FROM" \
	--to "$CONTACTEMAILS" \
	--subject "$SUBJECT" \
	--text "$TEXTMSG"
