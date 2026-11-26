FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace
COPY . .
RUN ./mvnw --batch-mode --no-transfer-progress -pl gateway -am clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder --chown=10001:10001 /workspace/gateway/target/gateway.jar /app/app.jar

USER 10001:10001
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
