# Use an official OpenJDK runtime as a parent image
# Make sure to use a Java version compatible with your project (Java 21 as per your pom.xml)
FROM eclipse-temurin:21-jdk-jammy

# Set a working directory
WORKDIR /app

# Argument for the JAR file name (makes it easier to update if version changes)
# Your pom.xml produces: trip-planner-backend-0.0.1-SNAPSHOT.jar
ARG JAR_FILE=target/jar/travel.jar

# Copy the executable JAR from the target folder to our /app directory
COPY ${JAR_FILE} app.jar

# Make port 8080 available to the world outside this container
# Render will map its PORT environment variable to this.
EXPOSE 8080

# Run the JAR file
# The PORT environment variable will be supplied by Render and automatically picked up by Spring Boot.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]