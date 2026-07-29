FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY src src

RUN mvn -B clean package -DskipTests


FROM registry.access.redhat.com/ubi8/openjdk-17:1.19

ENV LANGUAGE="en_US:en"

COPY --from=build --chown=185 /workspace/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /workspace/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /workspace/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /workspace/target/quarkus-app/quarkus/ /deployments/quarkus/

USER 185

ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

EXPOSE 10000

ENTRYPOINT ["/opt/jboss/container/java/run/run-java.sh"]
