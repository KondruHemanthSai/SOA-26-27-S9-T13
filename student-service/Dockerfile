# Multi-stage build for Student Service
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

ENV MAVEN_OPTS="-Xmx384m"

# Copy root pom and module poms
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY student-service/pom.xml student-service/
COPY admission-service/pom.xml admission-service/
COPY course-service/pom.xml course-service/
COPY registration-service/pom.xml registration-service/
COPY schedule-service/pom.xml schedule-service/
COPY notification-service/pom.xml notification-service/
COPY ai-service/pom.xml ai-service/

# Copy source code and build
COPY student-service/src student-service/src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -pl student-service -am

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app

COPY --from=builder /workspace/student-service/target/*.jar app.jar

ENV JAVA_OPTS="-Xmx384m -Xms128m"
EXPOSE 8082

HEALTHCHECK --interval=10s --timeout=5s --retries=5 --start-period=30s \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
