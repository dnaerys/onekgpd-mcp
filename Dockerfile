# Stage 1: build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
RUN ./mvnw package -B -DskipTests -Dquarkus.package.jar.type=uber-jar

# Stage 2: runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN addgroup --system javauser && adduser --system --group javauser
USER javauser
COPY --from=build /app/target/onekgpd-mcp-runner.jar onekgpd-mcp-runner.jar
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 9000
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar onekgpd-mcp-runner.jar"]
