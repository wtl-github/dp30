
CLASSPATH=""
for jar in `ls /home/zjgzs/code/DBC3P0/lib/*.jar`
do
CLASSPATH="$CLASSPATH":"$jar"
done
CLASSPATH=$CLASSPATH:.:/home/zjgzs/code/DBC3P0/bin/

JAVA_HOME="/home/zjgzs/jdk/jdk1.8.0_144"

TARGET="DBC3P0"

serverName="DBC3P0"

for pid in `ps -ef | grep "target=${serverName}" | grep -v "grep" | awk ' { print $2 } '`
do
kill -9 $pid;
echo $pid;
done

nohup ${JAVA_HOME}/bin/java -Dtarget=${TARGET} -classpath ${CLASSPATH} leo.Main >> system.log 2>&1 &