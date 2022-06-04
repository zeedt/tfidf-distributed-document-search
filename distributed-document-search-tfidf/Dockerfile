FROM openjdk:11
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY ./resources/books books
ENTRYPOINT ["java","-jar","/app.jar"]