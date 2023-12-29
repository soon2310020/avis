#!/bin/bash

IDP_HOST=testidp.kreonet.net
IDP_PORT=443
CERTIFICATE_FILE=ssocircle.cert
KEYSTORE_FILE=samlKeystore.jks
KEYSTORE_PASSWORD=nalle123

#openssl s_client -host $IDP_HOST -port $IDP_PORT -prexit -showcerts </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > $CERTIFICATE_FILE
#keytool -delete -alias ssocircle -keystore $KEYSTORE_FILE -storepass $KEYSTORE_PASSWORD
#keytool -import -alias ssocircle -file $CERTIFICATE_FILE -keystore $KEYSTORE_FILE -storepass $KEYSTORE_PASSWORD -noprompt
#
#rm $CERTIFICATE_FILE

keytool -genkey -alias kreonet -validity 3650 -keyalg RSA -sigalg SHA256withRSA -keysize 2048 -keystore samlKeystore.jks -keypass nalle123 -storepass nalle123 -dname "CN=testidp.kreonet.net,OU=dbclose,O=dbclose,L=Gurogu,S=Seoul,C=KR"