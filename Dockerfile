# First stage, build the application
FROM openjdk:17-jdk-alpine AS TEMP_BUILD_IMAGE
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build -x test
COPY build/libs/*.jar build/libs/cloud-file-storage.jar

# Second stage, build a docker image with output artifact from previous stage.
FROM openjdk:17-jdk-alpine
ENV ARTIFACT_NAME=cloud-file-storage.jar

ENV APP_HOME=/usr/app

ENV JDBC_ROOT=root
ENV JDBC_PASSWORD=root
ENV JDBC_URL=jdbc:mysql://mysql:3306/file_storage

ENV REDIS_HOST=redis_container
ENV REDIS_PORT=6379
ENV REDIS_PASSWORD=redis-password

ENV MINIO_URL=http://file-storage-minio:9000
ENV MINIO_USER=minio-user
ENV MINIO_PASSWORD=password

WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .
EXPOSE 8080
CMD "java" "-jar" ${ARTIFACT_NAME}