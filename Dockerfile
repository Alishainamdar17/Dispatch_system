FROM eclipse-temurin:17-jdk

WORKDIR /app

# Ensure the file path is correct. If using Maven, it should be in 'target'
COPY target/onedeoleela-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]