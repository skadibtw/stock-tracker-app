FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY mobile-api ./mobile-api
COPY mobile-backend ./mobile-backend

RUN sed -i 's/\r$//' ./gradlew
RUN chmod +x ./gradlew
RUN ./gradlew :mobile-api:installDist --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/mobile-api/build/install/mobile-api ./

EXPOSE 8081

USER 10001

ENTRYPOINT ["/app/bin/mobile-api"]
