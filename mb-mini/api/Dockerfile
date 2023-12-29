FROM eclipse-temurin:11.0.20.1_1-jre

RUN apt-get update -y 
RUN apt-get install -y libfreetype6
        
#CHANGE THIS
COPY ./target/mini-application-1.0.1-SNAPSHOT.jar /opt/app.jar
    
EXPOSE 8080

ENV TZ=Asia/Ho_Chi_Minh

ENTRYPOINT ["java","-jar","/opt/app.jar"]
