# First stage, build the application
FROM openjdk:17-jdk-alpine AS TEMP_BUILD_IMAGE
WORKDIR /usr/app

COPY build.gradle settings.gradle gradle.properties gradlew /usr/app/
COPY gradle /usr/app/gradle

RUN chmod +x gradlew && ./gradlew dependencies || return 0

COPY . .

RUN ./gradlew build -x test

# Second stage, build a docker image with output artifact from previous stage.
FROM openjdk:17-jdk-alpine
ENV APP_HOME=/usr/app

ENV JDBC_ROOT=root
ENV JDBC_PASSWORD=root
ENV JDBC_URL=jdbc:mysql://mysql:3306/file_storage

ENV REDIS_HOST=redis-container
ENV REDIS_PORT=6379
ENV REDIS_PASSWORD=redis-password

ENV MINIO_URL=http://file-storage-minio:9000
ENV MINIO_USER=minio-user
ENV MINIO_PASSWORD=password

WORKDIR $APP_HOME
RUN ls -al $APP_HOME
COPY --from=TEMP_BUILD_IMAGE /usr/app/build/libs/cloud-file-storage-0.0.1-SNAPSHOT.jar $APP_HOME/build/libs/cloud-file-storage.jar
EXPOSE 8080
