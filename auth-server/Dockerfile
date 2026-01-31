FROM eclipse-temurin:21-jre
ARG JAR_FILE=target/auth-server-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 9000
ENTRYPOINT ["java","-jar","/app.jar"]
