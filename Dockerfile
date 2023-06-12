FROM maven:latest as builder
WORKDIR /srv/build
COPY . .
RUN mvn package

FROM amazoncorretto:20-alpine
COPY --from=builder /srv/build/target/ResourcePackValidator.jar /usr/lib/resourcepackvalidator/ResourcePackValidator.jar
COPY ./docker/resourcepackvalidator /usr/bin/resourcepackvalidator