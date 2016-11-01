#! /bin/bash 

NOTIFICATIONTYPE="$1"
HOSTNAME="$2"
HOSTSTATE="$3"
HOSTADDRESS="$4"
HOSTOUTPUT="$5"
LONGDATETIME="$6"
CONTACTEMAILS="$7"

SERVICEOUTPUT=$(echo "$SERVICEOUTPUT" | sed -e 's/br\//\n/g')

msg=$(cat <<-EOF
***** Nagios *****

Notification Type: $NOTIFICATIONTYPE
Host: $HOSTNAME
State: $HOSTSTATE
Address: $HOSTADDRESS
Info: $HOSTOUTPUT

Date/Time: $LONGDATETIME
EOF
)

FROM="$TODO_FROM_EMAIL_ADDRESS"
SUBJECT="** $NOTIFICATIONTYPE Host Alert: $HOSTNAME is $HOSTSTATE **"

aws --region $TODO_AWS_REGION ses send-email \
	--from "$FROMEMAIL" \
	--to "$CONTACTEMAILS" \
	--subject "$SUBJECT" \
	--text "$msg"
