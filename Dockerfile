FROM openjdk:21-ea

# Set Working directory
WORKDIR /app

# Copy the JAR from the target folder

ARG JAR_FILE=target/*SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Expose the port the app will run
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
