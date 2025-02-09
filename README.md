```text
    _   __                     __  __                           _    __     
   / | / /___  ____  ____ _   / / / /___  ____ _____  ____ _   | |  / /_  __
  /  |/ / __ \/ __ \/ __ `/  / /_/ / __ \/ __ `/ __ \/ __ `/   | | / / / / /
 / /|  / /_/ / / / / /_/ /  / __  / /_/ / /_/ / / / / /_/ /    | |/ / /_/ / 
/_/ |_/\____/_/ /_/\__, /  /_/ /_/\____/\__,_/_/ /_/\__, /     |___/\__,_/  
                  /____/                           /____/                   
Backend service for learning
Start date: 23/01/2025
Test Jenkin - 1 + 1

Website: https://bit.ly/nong-hoang-vu-java
Youtube: https://www.youtube.com/@javatech04
Facebook: https://www.facebook.com/NongHoangVu04
```
## Prerequisite
- Cài đặt JDK 17+
- Install Maven 3.5+
- Install IntelliJ

## Technical Stacks
- Java 17
- Spring Boot 3.2.3
- Maven 3.5+
- Spring boot 3.4.2
- Spring actuator
- Lombok
- DevTools
- Docker
- Docker compose

## Create project
- [Spring Initializr](https://start.spring.io/)
- [Banner](https://devops.datenkollektiv.de/banner.txt/index.html)

## Build & Run Application
- Run application with **mvnw** at folder **backend-service**
```bash
./mvnw spring-boot:run
```
- Run application by docker
```bash
$ mvn clean install -P dev
$ docker build -t backend-service:latest .
$ docker run -it -p 8080:8080 --name backend-service backend-service:latest
```

## Test
- Check health with **cURL**
```bash
curl --location 'http://localhost:8080/actuator/health'
-- Response --
{
  "status": "UP"
} 
```
- Use swagger UI to test [Run swagger UI](http://localhost:8080/swagger-ui/index.html)
