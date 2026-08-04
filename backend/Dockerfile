FROM maven:3.9.9-eclipse-temurin-17
WORKDIR /app
COPY . .
RUN mvn -q -DskipTests package
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/seat-booking-backend-0.0.1-SNAPSHOT.jar"]
