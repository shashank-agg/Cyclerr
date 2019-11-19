# Cyclerr

This repository contains all implementation related to the "Cyclerr" projected implemented for 
IN4398 Advanced Practical IoT and Seminar (2019/20 Q1).

## Overview of Content
* [Presentation](presentation.pdf)
* [Video](video.mp4)
* [Cyclerr App](android)
* [Cyclerr Back End](backend)
* [Cyclerr Web App](webapp)

## Guides

### Android

For running this project use [Android Studio](https://developer.android.com/studio) and make sure that you give the app all permissions (e.g. GPS) when asked. 

### Back End

The Cyclerr backend is based on a template https://start.spring.io/. It is based on Spring Boot and requires a MongoDB to store the data. The important source code can be found in `src/main/java`

Commands:
* Start locally: `./gradlew bootRun`
* Build `./gradlew build`
* Build docker image `docker build .`

Endpoints:
* locally: http://localhost:8080/trips
* deployed: https://iot.nonnenmacher.dev/trips

## Web Application

The front end code is based on React.js and is using the material-kit-react. 

Commands:
* Load dependencies `npm install`
* Start localy `npm start`
* Build `npm build`
* Build docker image `docker build .`


