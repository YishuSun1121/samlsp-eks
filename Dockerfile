FROM maven:3.6.0-jdk-8-alpine AS build
# RUN apk update && \
#     apk upgrade && \
#     apk add --no-cache openssl nss-dev nss

COPY src /usr/src/app/src
COPY pom.xml /usr/src/app

WORKDIR /usr/src/app
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
RUN mvn package -X -Dmaven.test.skip=true

FROM openjdk:8-jdk-alpine

ENV PROFILE="default"

VOLUME /tmp
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
WORKDIR /home/appuser
COPY --from=build /usr/src/app/target/saml2-sp-*.jar /home/appuser/app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/home/appuser/app/app.jar","--spring.profiles.active=${PROFILE}"]
