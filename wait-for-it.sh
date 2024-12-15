#!/usr/bin/env bash
# wait-for-it.sh: wait for a service to become available

# Exit on error
set -e

TIMEOUT=30
QUIET=0
HOST=""
PORT=""
CMD=""

usage() {
    echo "Usage: $0 host:port [-t timeout] [-q] -- command"
    exit 1
}

# Parse command line arguments
while getopts "t:q" opt; do
  case $opt in
    t) TIMEOUT=$OPTARG ;;
    q) QUIET=1 ;;
    *) usage ;;
  esac
done
shift $((OPTIND - 1))

# Get the host and port
HOSTPORT=$1
HOST=$(echo $HOSTPORT | cut -d: -f1)
PORT=$(echo $HOSTPORT | cut -d: -f2)
shift

# The command to execute after waiting
CMD=$@

# Function to check if the service is available
wait_for_service() {
    if [ $QUIET -eq 0 ]; then
        echo "Waiting for $HOST:$PORT to be available..."
    fi

    SECONDS=0
    while ! nc -z $HOST $PORT; do
        sleep 1
        SECONDS=$((SECONDS + 1))
        if [ $SECONDS -ge $TIMEOUT ]; then
            echo "$HOST:$PORT did not become available in time."
            exit 1
        fi
    done

    if [ $QUIET -eq 0 ]; then
        echo "$HOST:$PORT is available."
    fi
}

# Wait for the service to be available
wait_for_service

# Execute the command after the service is available
exec $CMD
