# ─────────────────────────────────────────────────────────────
# ETAPA 1: COMPILACIÓN
# ─────────────────────────────────────────────────────────────
# 🔧 ADAPTAR: versión de Java = <java.version> del pom.xml
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src

RUN mvn clean package -DskipTests -q

# ─────────────────────────────────────────────────────────────
# ETAPA 2: JRE MÍNIMO CON JLINK
# ─────────────────────────────────────────────────────────────
# 🔧 ADAPTAR: misma versión de Java que la etapa 1
FROM eclipse-temurin:17-jdk-alpine AS jre-builder

RUN apk add --no-cache binutils

# 🔧 ADAPTAR: si agregas dependencias nuevas y la app falla al arrancar
# con ClassNotFoundException de una clase java.*/javax.*, esa clase te
# dice qué módulo agregar (ej. javax.management.JMException -> java.management)
RUN jlink \
    --add-modules java.base,java.compiler,java.desktop,java.management,java.naming,java.sql,jdk.unsupported,jdk.crypto.ec \
    --no-header-files \
    --no-man-pages \
    --compress=2 \
    --strip-debug \
    --output /jre-minimal

# strip: quita símbolos de depuración de las librerías nativas (-4 MB)
RUN find /jre-minimal -name '*.so*' -type f -exec strip --strip-unneeded {} +

RUN du -sh /jre-minimal

RUN mkdir -p /staging/lib && cp /lib/ld-musl-*.so.1 /staging/lib/

RUN mkdir -p /staging/tmp && chmod 1777 /staging/tmp

# ─────────────────────────────────────────────────────────────
# ETAPA 3: IMAGEN FINAL DESDE SCRATCH
# ─────────────────────────────────────────────────────────────
FROM scratch

COPY --from=jre-builder /jre-minimal /opt/java
COPY --from=jre-builder /staging/lib /lib
COPY --chown=1000:1000 --from=jre-builder /staging/tmp /tmp

COPY --from=builder /build/target/*.jar /app.jar

# 🔧 ADAPTAR: puerto y variable según el application.yaml del proyecto
# (aquí: server.port = ${APP_PORT:9095}). En el examen: 8091
ENV APP_PORT=9096

EXPOSE 9096

USER 1000:1000

ENTRYPOINT ["/opt/java/bin/java","-XX:MaxRAMPercentage=75.0","-jar","/app.jar"]
