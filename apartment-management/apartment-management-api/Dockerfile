FROM openjdk:17.0.2
LABEL authors="wangminan"
COPY target/apartment-management-api-1.0-SNAPSHOT.jar /app.jar
EXPOSE 63040
ENV TZ Asia/Shanghai
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dspring.profiles.active=aptmtprod"
ENTRYPOINT java ${JAVA_OPTS} -jar /app.jar
