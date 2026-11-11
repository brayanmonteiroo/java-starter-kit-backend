# syntax=docker/dockerfile:1
# Spring Boot 4.1+: jarmode=tools (layertools foi removido)
# https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles.html

################################################################################
# Resolve e baixa dependências (cache de camada + cache mount do Maven)
################################################################################
FROM eclipse-temurin:25-jdk AS deps

WORKDIR /build

COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/

RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -DskipTests

################################################################################
# Empacota o uber-jar
################################################################################
FROM deps AS package

WORKDIR /build

COPY ./src src/
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/application.jar

################################################################################
# Extrai layers do Spring Boot
################################################################################
FROM package AS extract

WORKDIR /build

RUN java -Djarmode=tools -jar target/application.jar extract --layers --destination target/extracted

################################################################################
# Desenvolvimento — JDWP na porta 8000
################################################################################
FROM extract AS development

WORKDIR /application

RUN cp -r /build/target/extracted/dependencies/. ./ \
 && cp -r /build/target/extracted/spring-boot-loader/. ./ \
 && cp -r /build/target/extracted/snapshot-dependencies/. ./ \
 && cp -r /build/target/extracted/application/. ./

ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000"

EXPOSE 8098 8000

CMD ["java", "-jar", "application.jar"]

################################################################################
# Produção — JRE mínimo, usuário non-root
################################################################################
FROM eclipse-temurin:25-jre AS final

ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser

WORKDIR /application

COPY --from=extract /build/target/extracted/dependencies/ ./
COPY --from=extract /build/target/extracted/spring-boot-loader/ ./
COPY --from=extract /build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/target/extracted/application/ ./

USER appuser

EXPOSE 8098

ENTRYPOINT ["java", "-jar", "application.jar"]
