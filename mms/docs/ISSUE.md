# eShotLink

## 터미널 등록 안됨.
- cause
```
The temporary upload location [tomcat.12141241...] is not valid
```
- tmp 디렉토리 설정으로 해결 
```
tmp83 > startup.sh 
 
-Djava.io.tmpdir=/tmp/tms83 으로 설정 
```

- application.properties 설정 변경 (확인필요 - 정상동작 되면 tmpdir 설정 대신 이 방법이 좋을 듯.)
```
If, like me, you arrived at this page because you're seeing this error while using Spring Boot, note that you can solve this issue by adding the following line to application.properties. Obviously you will substitute your own preferred tmp folder.

`spring.servlet.multipart.location=/home/ec2-user/boot/tmp`

More info in the [Spring 2.0 documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)

```

- https://adunhansa.tistory.com/209
- https://github.com/spring-projects/spring-boot/issues/5009
- 자세한 원인은 따로 확인해 볼것 .


