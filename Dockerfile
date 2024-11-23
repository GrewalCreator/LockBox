# # Stage 1: Build the project using Maven
# FROM maven:3.9.6 AS builder

# WORKDIR /app

# # Copy Maven POM and source code to the container
# COPY pom.xml .
# COPY src ./src

# # Run Maven to build the project and skip tests
# RUN mvn clean package -DskipTests

# # Stage 2: Run the application using Eclipse Temurin JDK 21 runtime
# FROM eclipse-temurin:21-jre

# RUN apt-get update && apt-get install -y \
#   openjfx \
#   x11-apps \
#   libxi6 \
#   libxrender1 \
#   libxrandr2 \
#   libglu1-mesa \
#   libgl1 \
#   libglx-mesa0 \
#   libosmesa6 \
#   --no-install-recommends



# # Set the username environment variable for creating a new user
# ENV USER_NAME=lockbox_user

# # Create a user with the lockbox_ prefix based on the environment variable USER_NAME
# RUN useradd -m -s /bin/bash $USER_NAME

# # Set the created user as the current user
# USER $USER_NAME

# WORKDIR /app

# # Copy the JAR file from the builder stage
# COPY --from=builder /app/target/lockbox-1.0-SNAPSHOT.jar ./lockbox.jar

# CMD ["java", "-Dprism.order=sw", "-Dprism.verbose=true", "--module-path", "/usr/share/openjfx/lib", "--add-modules", "javafx.controls,javafx.fxml", "-jar", "lockbox.jar"]










# # # Use the same base image you're working with
# # FROM eclipse-temurin:21-jre

# # # Install X11 utilities (x11-apps) and dependencies for minimal testing
# # RUN apt-get update && apt-get install -y \
# #     x11-apps \
# #     libxi6 \
# #     libxrender1 \
# #     libxrandr2 \
# #     libglu1-mesa \
# #     libgl1 \
# #     libglx-mesa0 \
# #     --no-install-recommends && \
# #     rm -rf /var/lib/apt/lists/*

# # # Set the working directory
# # WORKDIR /app

# # # Default command to run a simple X11 app (e.g., xeyes)
# # CMD ["xeyes"]



FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y \
  x11-apps \
  libxi6 \
  libxrender1 \
  libxrandr2 \
  libglu1-mesa \
  libgl1 \
  libglx-mesa0 \
  --no-install-recommends

CMD ["xeyes"]
