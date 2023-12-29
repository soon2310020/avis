#!/bin/sh

exec java -server -Dfile.encoding=UTF8 -XX:+UseG1GC -XX:+DisableExplicitGC -XX:+UseStringDeduplication \
 -Xms2048m -Xmx2048m \
 -Djava.net.preferIPv4Stack=true -Dcom.ning.http.client.AsyncHttpClientConfig.allowPoolingConnection=false \
 -Dspring.profiles.active=prod \
 -Daws.secretKey=snoRbZ4O9I7HdpB4+b3S2sD/ZeKOHUaQT/gNLDpO \
 -Daws.accessKeyId=AKIAZ47SBXAVM42A7FOV \
 -Dsaleson.mms.scheduling.enabled=true \
 -jar mms-1.0.0.jar