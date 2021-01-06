FROM openjdk:11
COPY target/*-with-dependencies.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]