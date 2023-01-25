@echo off

call gradlew assemble

docker build -t dev643/tbot:1.0 -t dev643/tbot:latest .

docker push dev643/tbot:1.0
docker push dev643/tbot:latest
