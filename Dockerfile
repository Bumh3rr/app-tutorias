FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build
RUN apt-get update -qq && apt-get install -y maven -qq
COPY pom.xml .
RUN mvn dependency:go-offline -B -q || true
COPY src ./src
RUN mvn clean package -DskipTests -B -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /build/target/app-tutorias-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]