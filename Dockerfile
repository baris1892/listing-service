# -----------------------------
# Build Stage
# -----------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Maven Wrapper + source code
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# -----------------------------
# Run Stage (slim)
# -----------------------------
FROM eclipse-temurin:21-jre
# or even use distroless docker image to get rid of many CVE issues
# FROM gcr.io/distroless/java21-debian12

WORKDIR /app
COPY --from=build /app/target/app.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
