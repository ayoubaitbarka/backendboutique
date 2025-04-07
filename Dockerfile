# Stage 1: Build WAR with Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
COPY . .
RUN mvn clean package

# Stage 2: Base image with Java 21
FROM tomcat:10.1-jdk21-temurin

# Clean default webapps
RUN rm -rf /usr/local/tomcat/webapps/*


# Copy the built WAR file to the Tomcat webapps folder with the correct name (boutique_war.war)
COPY --from=builder /app/target/boutique_war.war /usr/local/tomcat/webapps/boutique_war.war


# Expose backend port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
