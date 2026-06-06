.PHONY: checkstyle report test unit it up run-dev deploy

checkstyle:
	./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest
report:
	./gradlew jacocoTestReport
test:
	./gradlew test integrationTest
unit:
	./gradlew test
it:
	./gradlew integrationTest
up:
	docker compose -f docker-compose-env.yml up -d
run-dev:
	docker compose -f docker-compose-dev.yml up -d
deploy:
	docker compose -f docker-compose-prod.yml up -d mysql redis s3
	docker compose -f docker-compose-prod.yml build cfs-app
	docker compose -f docker-compose-prod.yml up -d --no-deps --force-recreate cfs-app
