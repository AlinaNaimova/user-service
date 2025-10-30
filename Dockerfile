FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/user-service-*.jar app.jar

RUN groupadd -r spring && useradd -r -g spring spring
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]