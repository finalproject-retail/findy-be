# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew clean bootJar -x test --no-daemon && \
    cp build/libs/*.jar app.jar

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

COPY --from=builder /app/app.jar app.jar

EXPOSE 8889

ENTRYPOINT ["java", "-jar", "app.jar"]
