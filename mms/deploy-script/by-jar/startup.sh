#!/bin/sh
TARGET=/home/emoldino/mms
mkdir $TARGET/logs
mkdir $TARGET/tomcat_upload_temp
cp $TARGET/logs/nohup.log $TARGET/nohup-`(date +%F_%H-%M-%S)`.log

nohup java -jar -Dspring.profiles.active=prod -Duser.timezone=Asia/Seoul -Djava.io.tmpdir=$TARGET/tomcat_upload_temp mms-1.0.0.jar -Xms512m -Xmx768m 1>$TARGET/nohup.out 2>&1 &

echo "MMS started.."



