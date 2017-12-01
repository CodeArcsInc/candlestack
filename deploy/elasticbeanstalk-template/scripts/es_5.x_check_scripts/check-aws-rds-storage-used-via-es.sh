#! /bin/bash

host=$1
authtoken=$2
instanceid=$3
warning=$4
critical=$5

# This function prints out an ES query
function get_query {
	cat <<-EOF
	{
		"query": {
			"bool": {
				"must": [
					{ "range": { "@timestamp": { "gte" : "now-20m", "lt" :  "now" } } },
					{ "term" : { "instanceId" : "$instanceid" } },
					{ "term" : { "type" : "aws_rds" } },
					{ "term" : { "metric_name" : "VolumeBytesUsed" } }
				]
			}
		},
		"size": 1,
		"sort": [{
			"@timestamp": {
				"order": "desc"
			}
		}]
	}
	EOF
}

# This function hits ES hosts with a specified query
function run_query {
	local query="$1"
	local today="$2"
	local yesterday="$3"
	local endpoint="https://$host/candlestack-$today,candlestack-$yesterday/_search?timeout=3m"

	local statuscode=$(curl -s -o /dev/null -w "%{http_code}" "https://$host/candlestack-$today" -H "Authorization: Basic $authtoken")
	if [ "$statuscode" == "404" ];then
		endpoint="https://$host/candlestack-$yesterday/_search?timeout=3m"
	fi

	curl -sm 10 "$endpoint" \
		-H "Authorization: Basic $authtoken" \
		-H 'Content-Type: application/json;charset=UTF-8' \
		-H 'Accept: application/json, text/plain, */*' \
		--data-binary @- <<< "$query" |
			# Get the result in CSV
			jq -r '.hits.hits[]._source | [.metric_name,.metric_value,.timestamp]|@csv'
}

function clean_input {
	local input="$1"
	# Remove double quotes, we're not going to need them
	sed -re 's/"//g' <<< "$input"  
}

# Creates message with line break "\n" if terminal or "<br/>" for better html visualization
function log_msg {
    if [ -t 1 ];then
            linebreak="\n"
    else
            linebreak="<br/>"
    fi

    if [ -z "$msg" ];then
        msg="$@"
    else
        msg="${msg}${linebreak}${@}"
    fi
}

# Get correct exit code based on message
function get_exit_code {
    msg1=$(echo -ne "$msg" | sed -e 's/<br\/>/\n/g')
    echo -e "$msg1" | grep -q "^CRITICAL" 
    if [ "$?" == 0 ];then
        export exit_code=2
        return $exit_code
    fi
    echo -e "$msg1" | grep -q "^WARNING"
    if [ "$?" == 0 ];then
        export exit_code=1
        return $exit_code
    fi
    echo -e "$msg1" | grep -q "^UNKNOWN"
    if [ "$?" == 0 ];then
        export exit_code=3
        return $exit_code
    fi
    export exit_code=0
    return
}

function print_msg_and_exit {
        echo -e "$msg"
        get_exit_code
        exit "$exit_code"
}

# We are using this function because test in bash can only test integers
function check_exp {
	local exp=$1
	local result=$(bc <<< "$exp")
	test "$result" -eq 1 
}

query=$(get_query)

input=$(run_query "$query" $(date +"%Y.%m.%d") $(date --date="yesterday" +"%Y.%m.%d"))

test -z "$input" && {
	log_msg "UNKNOWN: Plugin failed to retrieve input"
	print_msg_and_exit
}

while read line; do
	# This line creates 3 variables metric_name, metric_value and timestamp
	eval $(awk -F, '{printf "metric_name=%s metric_value=%s timestamp=%s\n",$1,$2,$3}' <<< "$line")
	
	if  check_exp "$metric_value <= $warning" ;then
		log_msg "OK: Storage Used = $metric_value bytes"
	elif check_exp "$metric_value > $warning && $metric_value <= $critical" ;then
		log_msg "WARNING: Storage Used = $metric_value bytes"
	elif check_exp "$metric_value > $critical"  ;then
		log_msg "CRITICAL: Storgae Used = $metric_value bytes"
	else 
		log_msg "UNKNOWN: Could not determine storage used"
	fi
	
done < <( clean_input "$input")

print_msg_and_exit
