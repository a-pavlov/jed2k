#!/bin/sh

# http://www.pgtk.edu.ru/gooamoko/doku.php?id=java:daemon
# Setup variables
EXEC=$HOME/local/bin/jsvc
JAVA_HOME="/usr/lib/jvm/default-java"
CLASS_HOME="$HOME/dev/jed2k/core/target"
CLASS_PATH="${CLASS_HOME}/byte-buddy-1.6.9.jar:${CLASS_HOME}/byte-buddy-agent-1.6.9.jar:${CLASS_HOME}/objenesis-2.5.jar:${CLASS_HOME}/slf4j-jdk14-1.7.21.jar:${CLASS_HOME}/slf4j-api-1.7.21.jar:${CLASS_HOME}/re2j-1.0.jar:${CLASS_HOME}/gson-2.7.jar:${CLASS_HOME}/okhttp-2.7.5.jar:${CLASS_HOME}/okio-1.6.0.jar:${CLASS_HOME}/lombok-1.16.10.jar:${CLASS_HOME}/postgresql-42.0.0.jre7.jar:${CLASS_HOME}/commons-daemon-1.0.15.jar:${CLASS_HOME}/commons-cli-1.2.jar:${CLASS_HOME}/jed2k-0.0.1.jar"
CLASS=org.dkf.jed2k.KadDaemon
OPTIONS="--port=20206 --host=localhost --database=test --user=test --password=test"
USER=apavlov
PID=/tmp/jed2k.pid
LOG_OUT=$CLASS_HOME/daemon.log
LOG_ERR=$CLASS_HOME/daemon.err
PROCNAME=jed2k-0.0.1
LOGGING="-Djava.util.logging.config.file=/home/apavlov/dev/jed2k/core/src/main/resources/logging.properties"
 
#-outfile $LOG_OUT -errfile $LOG_ERR -pidfile

do_exec()
{
    $EXEC -debug $LOGGING -home $JAVA_HOME -procname $PROCNAME -cp $CLASS_PATH  -pidfile $PID $1 $CLASS $OPTIONS
}

case "$1" in
    start)
        do_exec
            ;;
    stop)
        do_exec "-stop"
            ;;
    restart)
        if [ -f "$PID" ]; then
            do_exec "-stop"
            do_exec
        else
            echo "service not running."
            exit 1
        fi
            ;;
    *)
            echo "usage: daemon {start|stop|restart}" >&2
            exit 3
            ;;
esac
