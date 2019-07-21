#!/bin/sh

PID_FILE=".pid"

help() {
    echo -e "Usage: aiops -options"
    echo -e "where options include:"
    echo -e "\t-s | --start\t\tstart AIOps web service"
    echo -e "\t-k | --stop\t\tstop AIOps web service"
    echo -e "\t-r | --restart\t\trestart AIOps web service"
    echo -e "\t-p | --status\t\tshow AIOps web service status"
    echo -e "\t-h | --help\t\tprint this help message"
}

start() {
    jar=$(find . -maxdepth 1 -name "aiops-webservice-*.jar" -print -quit)
    # for de
    # $JAVA_HOME/bin/java -Dloader.path=config/:/etc/hdfs1/conf/ -jar "$jar" &> aiops.log &
    # for aiops
    $JAVA_HOME/bin/java -jar "$jar" &> aiops.log &
    echo "$!" > .pid
    echo "AIOps started"
}

existPID() {
    if [[ ! -f "$PID_FILE" ]]; then return 1; fi
    pid=$(head -n1 "$PID_FILE")
    kill -s 0 "$pid"
    if [[ "$?" == 0 ]]; then return 0; else return 1; fi
}

stop() {
    existPID
    if [[ "$?" == 0 ]]
    then
        pid=$(head -n1 "$PID_FILE")
        rm -f "$PID_FILE"
        kill -9 "$pid"
        echo "AIOps stopped"
        return 0
    else
        echo "AIOps process not found"
        return 1
    fi
}

status() {
    existPID
    if [[ "$?" == 0 ]]
    then
        pid=$(head -n1 "$PID_FILE")
        echo "AIOps running at pid=$pid"
    else
        echo "AIOps not running"
    fi
}

case $1 in
    -h|--help)
        help
        ;;
    -s|--start)
        start
        ;;
    -k|--stop)
        stop
        ;;
    -r|--restart)
        stop
        if [[ "$?" == 0 ]]
        then
            start
            echo "AIOps restarted"
        else
            echo "AIOps cannot be restarted"
        fi
        ;;
    -p|--status)
        status
        ;;
    -?*)
        echo "Unknown options"
        help
        ;;
    *)
        help
        ;;
esac
