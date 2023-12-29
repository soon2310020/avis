#!/bin/sh

echo "=============================="
echo "=== MMS Deploy - Dev Ohio =="
echo "=============================="

BUILD_BRANCH=pipeline/safe_develop
TARGET=/home/emoldino/mms/mms
BUILD_PATH=/home/emoldino/mms/mms/build/libs
TMPDIR=/home/emoldino/tomcat_upload_temp
PEFIX_LOG_NAME=nohup
LOG_PATH=/home/emoldino/mms/mms/logs/

echo "TO DO: cp build/libs/mms-1.0.0.jar $TARGET/mms-1.0.0.jar "

cd $TARGET
sudo kill `cat mms.pid`
sleep 2

echo "CURRENT_SERVER_ADDRESS=(hostname -I)"
echo "CURRENT_SERVER_ADDRESS : CURRENT_SERVER_ADDRESS"

sudo nohup java -jar -Dspring.profiles.active=prod -Duser.timezone=Asia/Seoul -Djava.io.tmpdir=$TMPDIR ${BUILD_PATH}/mms-1.0.0.jar -Xms512m -Xmx768m 1>${LOG_PATH}$PEFIX_LOG_NAME.log 2>&1 &

echo "MMS started.."



