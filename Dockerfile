FROM openjdk:21-jdk-slim AS build
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

COPY src src
RUN ./mvnw clean package -DskipTests

FROM openjdk:21-jre-slim
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
