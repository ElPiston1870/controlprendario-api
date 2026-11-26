# syntax=docker/dockerfile:1

FROM --platform=$BUILDPLATFORM node:22-bookworm-slim AS frontend
WORKDIR /build/frontend
COPY controlprendario-frontend/package.json controlprendario-frontend/package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --no-audit --no-fund
COPY controlprendario-frontend/angular.json controlprendario-frontend/tsconfig.json controlprendario-frontend/tsconfig.app.json controlprendario-frontend/server.ts ./
COPY controlprendario-frontend/src ./src
COPY controlprendario-frontend/public ./public
RUN npm run build -- --configuration production

FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /build/backend
COPY controlprendario-api/pom.xml ./
COPY controlprendario-api/src/main ./src/main
COPY --from=frontend /build/frontend/dist/contro-prendario/browser/ ./src/main/resources/static/
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B -ntp -DskipTests package && cp target/prendario-*.jar /build/app.jar

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=backend --chown=10001:10001 /build/app.jar ./app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_ADDRESS=0.0.0.0 \
    SERVER_PORT=8080 \
    JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -Duser.timezone=America/Bogota"

USER 10001:10001
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=4s --start-period=40s --retries=3 \
  CMD ["curl", "--fail", "--silent", "--show-error", "--output", "/dev/null", "--max-time", "3", "http://127.0.0.1:8080/index.html"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
