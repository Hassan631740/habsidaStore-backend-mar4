# Build stage: Maven + JDK 21
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy POM and download dependencies (cached unless POM changes)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the application
COPY src ./src
RUN mvn package -DskipTests -B

# Run stage: JRE only
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root
RUN adduser -D -u 1000 appuser
USER appuser

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
