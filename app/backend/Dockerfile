# Use a base image with Maven installed
FROM maven:latest

# Set the working directory inside the container
WORKDIR /app

# Copy the project's pom.xml file to the container
COPY pom.xml .

# Download the project dependencies
RUN mvn dependency:resolve

# Copy the project source code to the container
COPY src/ ./src/

# Build the project
RUN mvn package

# Set the startup command to run the application
CMD ["java", "-jar", "target/App.jar"]
