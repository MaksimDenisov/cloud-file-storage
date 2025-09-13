THIS_FILE := $(lastword $(MAKEFILE_LIST))
.PHONY: help build up start down destroy stop restart logs logs-api ps login-timescale login-api db-shell

checkstyle:
	./gradlew checkstyleMain checkstyleTest
report:
	./gradlew jacocoTestReport

test:
	./gradlew test
it:
	./gradlew integrationTest

up:
	docker-compose -f docker-compose-env.yml up -d

run-dev:
	docker-compose -f docker-compose-dev.yml up -d

run-app-docker:
	docker build --tag 'cloud-file-storage' .
	docker run --detach -p 8080:8080  --network cloud-file-storage_internal_network 'cloud-file-storage'

deploy:
	docker-compose -f docker-compose-prod.yml up -d mysql redis s3
	docker-compose -f docker-compose-prod.yml build cfs-app
	docker-compose -f docker-compose-prod.yml up -d --no-deps --force-recreate cfs-app
