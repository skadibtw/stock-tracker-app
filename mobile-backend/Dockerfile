FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY mobile-backend ./mobile-backend
COPY mobile-api ./mobile-api

RUN sed -i 's/\r$//' ./gradlew
RUN chmod +x ./gradlew
RUN ./gradlew :mobile-backend:installDist --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/mobile-backend/build/install/mobile-backend ./

EXPOSE 8080

USER 10001

ENTRYPOINT ["/app/bin/mobile-backend"]
