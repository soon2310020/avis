#!/bin/bash

# IDP metadata 에서 sharing / encrypt 내용을 복사하새 인증서(kreonet.cer)를 만들고
# samlKeystore.jks에 아래 방법으로 import 한다.
keytool -importcert -keystore samlKeystore.jks -storepass nalle123 -alias kreonet -file kreonet.cer

