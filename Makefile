THIS_FILE := $(lastword $(MAKEFILE_LIST))
.PHONY: help build up start down destroy stop restart logs logs-api ps login-timescale login-api db-shell

checkstyle:
	./gradlew checkstyleMain checkstyleTest
up:
	docker-compose -f docker-compose.yml up -d
report:
	./gradlew jacocoTestReport