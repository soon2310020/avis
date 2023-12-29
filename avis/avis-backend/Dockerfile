FROM openjdk:8-jre
WORKDIR /app
ARG JAR_PATH
COPY $JAR_PATH /app/app.jar

ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active="$PROFILES" -jar /app/app.jar