# syntax=docker/dockerfile:1.7

# ============================================
# ETAPA 1: BUILD
# ============================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace/app

# Copiar primero Gradle Wrapper y configuración
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x gradlew

# Copiar el código fuente
COPY src ./src

# Compilar usando caché persistente de BuildKit
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar -x test --no-daemon

# Extraer el JAR para separar dependencias y clases
RUN mkdir -p build/dependency \
    && cd build/dependency \
    && jar -xf ../libs/app.jar


# ============================================
# ETAPA 2: RUNTIME
# ============================================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# curl se utiliza exclusivamente para el health check
RUN apk add --no-cache curl \
    && addgroup -S spring \
    && adduser -S spring -G spring

ARG DEPENDENCY=/workspace/app/build/dependency

# Dependencias externas: cambian con menor frecuencia
COPY --from=builder --chown=spring:spring \
    ${DEPENDENCY}/BOOT-INF/lib /app/lib

# Metadatos del JAR
COPY --from=builder --chown=spring:spring \
    ${DEPENDENCY}/META-INF /app/META-INF

# Clases y recursos de la aplicación
COPY --from=builder --chown=spring:spring \
    ${DEPENDENCY}/BOOT-INF/classes /app

USER spring:spring

EXPOSE 8080

# Configuración no sensible y reemplazable en runtime
ENV TZ=America/Guayaquil

HEALTHCHECK --interval=30s \
    --timeout=5s \
    --start-period=60s \
    --retries=3 \
    CMD curl --fail --silent --show-error \
    http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx512m", \
    "-cp", \
    "/app:/app/lib/*", \
    "ec.edu.ups.icc.fundamentos01.Fundamentos01Application"]