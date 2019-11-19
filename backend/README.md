# Cyclerr Back End


This folder contains the source code of the backend system. The project was templated by using https://start.spring.io/.
It is based on Spring Boot and uses a MongoDB to store the data.


## Commands
The jar file can be build with: `./gradlew bootRun`
Then the app can be started with calling `java -jar build/libs/cyclerr-backend-0.0.1-SNAPSHOT.jar`

## Endpoints
The REST-API for the web app can be found at http://localhost:8080/trips
Additionally the API is deployed and can be called : https://iot.nonnenmacher.dev/trips

## Docker
The dockerfile can be build with `docker build .`
