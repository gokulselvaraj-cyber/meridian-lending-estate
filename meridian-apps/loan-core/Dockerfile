FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/loan-core-4.2.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
