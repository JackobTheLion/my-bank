FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace
COPY . .
RUN ./mvnw --batch-mode --no-transfer-progress -pl transfer -am clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder --chown=10001:10001 /workspace/transfer/target/transfer.jar /app/app.jar

USER 10001:10001
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
