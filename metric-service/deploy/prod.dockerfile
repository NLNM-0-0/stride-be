FROM maven:3.8-openjdk-17 AS build
WORKDIR /app

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

RUN mkdir -p /root/.m2 && \
    echo "<settings>\
        <servers>\
            <server>\
                <id>github-dto</id>\
                <username>\${GITHUB_USERNAME}</username>\
                <password>\${GITHUB_TOKEN}</password>\
            </server>\
            <server>\
                <id>github-common</id>\
                <username>\${GITHUB_USERNAME}</username>\
                <password>\${GITHUB_TOKEN}</password>\
            </server>\
        </servers>\
    </settings>" > /root/.m2/settings.xml && \
    sed -i "s/\${GITHUB_USERNAME}/$GITHUB_USERNAME/g" /root/.m2/settings.xml && \
    sed -i "s/\${GITHUB_TOKEN}/$GITHUB_TOKEN/g" /root/.m2/settings.xml

COPY pom.xml ./
COPY src ./src/
COPY mvnw ./
COPY .mvn ./.mvn/

RUN chmod +x mvnw && \
    ./mvnw clean package -DskipTests && \
    rm -rf /root/.m2 /root/.mvn

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/metric-service-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]