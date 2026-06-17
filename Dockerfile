# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and the wrapper files
COPY . .

# ---> THE FIX: Grant execution rights to the Maven wrapper <---
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Copy the jar file from the target directory to the root of the container
RUN cp target/*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]