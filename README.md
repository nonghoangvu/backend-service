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

## Spring Boot with Elasticsearch

### 1. Build database elasticsearch with `docker-compose.yml`

```yaml
  elastic-search:
    image: elasticsearch:7.14.1
    container_name: elasticsearch
    restart: always
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
```

### 2. Configure ElasticSearch in Spring Boot

#### a. Add dependency for elasticsearch

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

#### b. Connect configuration in `application.properties` or `application.yml`

```properties
spring.elasticsearch.uris=http://localhost:9200
#spring.elasticsearch.username=elastic
#spring.elasticsearch.password=changeme
```

###### Note: Because it does not set up authentication, no username/password

#### c. Create document and repository for elasticsearch

- UserDocument

```java
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import vn.vunh.common.Gender;

import java.util.Date;

@Getter
@Setter
@Builder
@Document(indexName = "users")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntityDocument {
    @Id
    private String id;

    private String fullName;

    private Gender gender;

    @Temporal(TemporalType.DATE)
    private Date birthday;

    private String username;

    private String email;

    private String phone;
}
```

- UserSearchRepository

```java
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import vn.vunh.model.elasticsearch.UserEntityDocument;

@Repository
public interface UserEntityDocumentRepository extends ElasticsearchRepository<UserEntityDocument, String> {
    UserEntityDocument findUserEntityDocumentByEmail(String email);
}
```

### 3. Configure postgres in Spring Boot

#### a. Add dependency for postgresql

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### b. Connect configuration in `application.properties` or `application.yml`
```yml
spring:
  datasource:
    url: jdbc:postgresql://${DATABASE_HOST:localhost}:${DATABASE_PORT:5431}/hoangvu
    username: ${DATABASE_USERNAME:hoangvu}
    password: ${DATABASE_PASSWORD:321}
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true
    hibernate:
      ddl-auto: none
```

#### c. Use JPA for postgresql
- UserEntity
```java
@Getter
@Setter
@Entity
@Table(name = "tbl_user")
@Slf4j(topic = "UserEntity")
public class UserEntity extends AbstractEntity<Long> implements UserDetails, Serializable {

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date birthday;

    @Column(name = "email", length = 255, unique = true)
    private String email;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "username", unique = true, nullable = false, length = 255)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", length = 255)
    private UserType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", length = 255)
    private UserStatus status;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<UserHasRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<GroupHasUser> groups = new HashSet<>();
}
```

- UserRepository
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vunh.model.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = "select u from UserEntity u where u.status='ACTIVE' " +
            "and (lower(u.firstName) like :keyword " +
            "or lower(u.lastName) like :keyword " +
            "or lower(u.username) like :keyword " +
            "or lower(u.phone) like :keyword " +
            "or lower(u.email) like :keyword)")
    Page<UserEntity> searchByKeyword(String keyword, Pageable pageable);

    UserEntity findByUsername(String username);

    UserEntity findByEmail(String email);
}
```