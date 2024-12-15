# Проект “облачное хранилище файлов”
Многопользовательское файловое облако. Пользователи сервиса могут использовать его для загрузки и хранения файлов.
----
#### Тех.задание.
https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/


[![Deploy](https://github.com/MaksimDenisov/cloud-file-storage/actions/workflows/deploy.yml/badge.svg)](https://github.com/MaksimDenisov/cloud-file-storage/actions/workflows/deploy.yml)

### Quality by Code Climate
[![Maintainability](https://api.codeclimate.com/v1/badges/d03079dc7c034b59a341/maintainability)](https://codeclimate.com/github/MaksimDenisov/cloud-file-storage/maintainability)

[![Test Coverage](https://api.codeclimate.com/v1/badges/d03079dc7c034b59a341/test_coverage)](https://codeclimate.com/github/MaksimDenisov/cloud-file-storage/test_coverage)

###  Prepare developing environment. Start docker containers(MySql, Minio, Redis).
```sh
make up
```

###  Start app in docker-container. Needs running environment. Start by "make up".
```sh
make run-app-docker
```

###  Used by Github Actions
```sh
make deploy
```

