# Project "Cloud File Storage"

## A multi-user file cloud. Service users can use it to upload and store files.

Technical Specifications:
https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/
## Badges
### Deploy status:
[![Deploy](https://github.com/MaksimDenisov/cloud-file-storage/actions/workflows/deploy.yml/badge.svg)](https://github.com/MaksimDenisov/cloud-file-storage/actions/workflows/deploy.yml)

# Quality by Sonar Qube:

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=MaksimDenisov_cloud-file-storage&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=MaksimDenisov_cloud-file-storage)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=MaksimDenisov_cloud-file-storage&metric=coverage)](https://sonarcloud.io/summary/new_code?id=MaksimDenisov_cloud-file-storage)
[![Maintainability](https://sonarcloud.io/api/project_badges/measure?project=MaksimDenisov_cloud-file-storage&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=MaksimDenisov_cloud-file-storage)
![SonarCloud](https://github.com/MaksimDenisov/cloud-file-storage/actions/workflows/sonar.yml/badge.svg)


### Quality by Code Climate:
[![Maintainability](https://api.codeclimate.com/v1/badges/d03079dc7c034b59a341/maintainability)](https://codeclimate.com/github/MaksimDenisov/cloud-file-storage/maintainability)

[![Test Coverage](https://api.codeclimate.com/v1/badges/d03079dc7c034b59a341/test_coverage)](https://codeclimate.com/github/MaksimDenisov/cloud-file-storage/test_coverage)


### This project used:
![Java](https://img.shields.io/badge/Java-007396?style=flat&logo=openjdk&logoColor=white)

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=spring&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=flat&logo=thymeleaf&logoColor=white)

![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat&logo=javascript&logoColor=white)

![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![Minio](https://img.shields.io/badge/MinIO-00B5E2?style=flat&logo=minio&logoColor=white)

![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)



## Makefile Commands

The following commands are available through the Makefile:

##### Prepare developing environment. Start docker containers(MySql, Minio, Redis).
```sh
make up
```

#####  Start app in docker-container. Needs running environment. Start by "make up".
```sh
make run-app-docker
```

## Environment Variables

The project requires several environment variables to be set. 
These can be added to a `.env` file in the root directory, or set directly in your terminal.
## Environment Variables

The project requires several environment variables to be set. These can be added to a `.env` file in the root directory, or set directly in your terminal.

| Variable Name       | Description                                      | Default Value if used "dev" profile    |
|---------------------|--------------------------------------------------|----------------------------------------|
| `JDBC_ROOT`          | Root username for JDBC connection.              | `root`                                 |
| `JDBC_PASSWORD`      | Password for JDBC connection.                   | `root`                                 |
| `JDBC_URL`           | URL for the MySQL database connection.          | `jdbc:mysql://mysql:3306/file_storage` |
| `REDIS_HOST`         | Host for the Redis instance.                    | `redis`                                |
| `REDIS_PORT`         | Port for the Redis instance.                    | `6379`                                 |
| `REDIS_PASSWORD`     | Password for Redis connection.                  | `redis-password`                       |
| `MINIO_URL`          | URL for the Minio instance.                     | `http://s3:9000`                       |
| `MINIO_USER`         | Minio username.                                  | `minio-user`                           |
| `MINIO_PASSWORD`     | Minio password.                                  | `password`                             |

## Installation

To get started with this project, clone the repository and install dependencies:

```bash
git clone https://github.com/username/project-name.git
cd cloud-file-storage 
make run-app-docker
```