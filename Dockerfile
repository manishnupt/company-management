FROM maven:3.8.4-openjdk-17-slim AS maven_cache

# Set the working directory in the container
WORKDIR /app

COPY pom.xml .

COPY src ./src
COPY target/company-management-0.0.1-SNAPSHOT.jar .
# Expose the port that the Spring Boot application will run on
EXPOSE 9910

# Define the command to run the Spring Boot application when the container starts
CMD ["java", "-jar", "company-management-0.0.1-SNAPSHOT.jar"]