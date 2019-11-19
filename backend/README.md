# Back End

The Cyclerr backend is based on a template https://start.spring.io/. It is based on Spring Boot and requires a MongoDB to store the data. The important source code can be found in `src/main/java`

Commands:
* Start locally: `./gradlew bootRun`
* Build `./gradlew build`
* Build docker image `docker build .`

Endpoints:
* locally: http://localhost:8080/trips
* deployed: https://iot.nonnenmacher.dev/trips
