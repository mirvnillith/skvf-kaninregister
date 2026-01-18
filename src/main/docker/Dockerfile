FROM ibmjava:jre

ARG JAR_FILE
COPY ${JAR_FILE} app.jar
COPY application.yaml application.yaml

ENTRYPOINT ["java","-jar","/app.jar"]