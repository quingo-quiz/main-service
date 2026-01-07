FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app
COPY /pom.xml .
COPY src ./srcd
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]