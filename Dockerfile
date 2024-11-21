# Stage 1: Build the project using Maven
FROM maven:3.9.6 AS builder

WORKDIR /app

# Copy Maven POM and source code to the container
COPY pom.xml .
COPY src ./src

# Run Maven to build the project and skip tests
RUN mvn clean package -DskipTests

# Stage 2: Run the application using Eclipse Temurin JDK 21 runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/lockbox-1.0-SNAPSHOT.jar ./lockbox.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "lockbox.jar"]
