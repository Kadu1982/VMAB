FROM gradle:8.14.3-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN gradle bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

# A imagem de runtime recebe curl para o healthcheck do container e um diretorio fixo de logs.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /app/logs

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
