#
# Build stage
#
#FROM maven:3.8.4-openjdk-17-slim as build
#COPY ./ ./
#RUN mvn -f pom.xml clean install -DskipTests

#
# Package stage
#
FROM openjdk:17-jdk-slim
COPY ./target/tools-0.0.1-SNAPSHOT.jar /tools-0.0.1-SNAPSHOT.jar
COPY ./target/monas.vaimee.it /



ENTRYPOINT ["java","-jar","tools-0.0.1-SNAPSHOT.jar"]