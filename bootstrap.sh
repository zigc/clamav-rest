#!/bin/bash
set -m

# start clam service itself and the updater in background as daemon
freshclam -d &
clamd &

# recognize PIDs
pidlist=`jobs -p`

# initialize latest result var
latest_exit=0


host=${CLAMD_HOST:-127.0.0.1}
port=${CLAMD_PORT:-3310}
filesize=${MAXSIZE:-20MB}

echo "using clamd server: $host:$port"

# start in background
#java -jar /var/clamav-rest/clamav-rest-1.0.2.jar --clamd.host=$host --clamd.port=$port
java -jar /var/clamav-rest/clamav-rest-1.0.2.jar --clamd.host=$host --clamd.port=$port --clamd.maxfilesize=$filesize --clamd.maxrequestsize=$filesize

# define shutdown helper
function shutdown() {
    trap "" SIGINT

    for single in $pidlist; do
        if ! kill -0 $single 2>/dev/null; then
            wait $single
            latest_exit=$?
        fi
    done

    kill $pidlist 2>/dev/null
}

# run shutdown
trap shutdown SIGINT
wait -n

# return received result
exit $latest_exit
