```text
    _   __                     __  __                           _    __     
   / | / /___  ____  ____ _   / / / /___  ____ _____  ____ _   | |  / /_  __
  /  |/ / __ \/ __ \/ __ `/  / /_/ / __ \/ __ `/ __ \/ __ `/   | | / / / / /
 / /|  / /_/ / / / / /_/ /  / __  / /_/ / /_/ / / / / /_/ /    | |/ / /_/ / 
/_/ |_/\____/_/ /_/\__, /  /_/ /_/\____/\__,_/_/ /_/\__, /     |___/\__,_/  
                  /____/                           /____/                   
Backend service for learning
Start date: 23/01/2025

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
- Spring Boot 3.3.4
- PostgresSQL
- Kafka
- Redis
- Maven 3.5+
- Lombok
- DevTools
- Docker, Docker compose

## Build application
```bash
mvn clean package -P dev|test|uat|prod
```

## Run application
- Maven statement
```bash
./mvnw spring-boot:run
```
- Jar statement
```bash
java -jar target/backend-service.jar
```

- Docker
```bash
docker build -t backend-service .
docker run -d backend-service:latest backend-service
```

## Package application
```bash
docker build -t backend-service .
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
## Trick Postman
```bash
var hackerVu = JSON.parse(responseBody);
if(hackerVu != null){
    postman.setEnvironmentVariable("access_token", hackerVu.accessToken);
    postman.setEnvironmentVariable("refresh_token", hackerVu.refreshToken);
}
```
