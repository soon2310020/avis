#Build
BUILD_BRANCH=deploy-internal-8085
TARGET=/home/emoldino/mms8085
PEFIX_LOG_NAME=nohup-8085
LOG_PATH=/var/logs/emoldino/dev/
TMPDIR=/home/emoldino/tomcat_upload_temp

sudo mkdir -vp $LOG_PATH && sudo chmod 777 -R $LOG_PATH

# Refresh Folder with newest source Code:
#eval `ssh-agent -s` && ssh-add ~/.ssh/healthhub_rsa
cd mms
#optional: init folder config
[ ! -d $TARGET/config ] && mkdir -vp $TARGET/config
[ ! -f ${TARGET}/shutdown.sh ] && cp deploy-script/by-jar/shutdown.sh $TARGET/shutdown.sh
[ ! -f ${TARGET}/config/application.properties ] && cp deploy-script/8085/config/application.properties $TARGET/config/application.properties

#sudo git fetch origin && sudo git stash save && sudo git checkout $BUILD_BRANCH && sudo git rebase origin/$BUILD_BRANCH
git fetch origin &&  git stash save &&  git checkout $BUILD_BRANCH && git pull

#install BE:
[ ! -d "build/generated-snippets" ] && mkdir -vp build/generated-snippets
#./gradlew build
./gradlew bootJar -x test
#sudo rm -rf /home/emoldino/tomcat_upload_temp/*
#############################################
#sudo fuser -n tcp -k $PORT || true

cp build/libs/mms-1.0.0.jar $TARGET/mms-1.0.0.jar
cd $TARGET
pwd
kill `cat mms.pid`
sleep 2
#cp ${LOG_PATH}$PEFIX_LOG_NAME.log ${LOG_PATH}$PEFIX_LOG_NAME-`(date +%F_%H-%M-%S)`.log
tar -czvf ${LOG_PATH}$PEFIX_LOG_NAME-`(date +%F_%H-%M-%S)`.gz ${LOG_PATH}$PEFIX_LOG_NAME.log
#optional: remove old log file
find ${LOG_PATH} -iname nohup* -mtime +30 -type f -delete

#nohup java -Xmx2048m -jar -Dspring.output.ansi.enabled=ALWAYS -Dspring.profiles.active=live /home/ec2-user/mms/build/libs/emoldino.myscanshare.war > /var/logs/emoldino/dev/dev-$BUILD_BRANCH.log 2>&1 &
nohup java -jar -Dspring.profiles.active=prod -Duser.timezone=Asia/Seoul -Djava.io.tmpdir=$TMPDIR ${TARGET}/mms-1.0.0.jar -Xms512m -Xmx768m 1>${LOG_PATH}$PEFIX_LOG_NAME.log 2>&1 &
echo "MMS started.."
echo "Show log:\n tail -f "${LOG_PATH}$PEFIX_LOG_NAME.log
