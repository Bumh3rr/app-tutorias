FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build
RUN apt-get update -qq && apt-get install -y maven -qq
COPY pom.xml .
RUN mvn dependency:go-offline -B -q || true
COPY src ./src
RUN mvn clean package -DskipTests -B -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
RUN mkdir -p /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]