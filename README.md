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

## Send mail using [SendGrid](https://app.sendgrid.com/)

### 1. Add dependency `pom.xml`
```xml
<dependency>
    <groupId>com.sendgrid</groupId>
    <artifactId>sendgrid-java</artifactId>
    <version>4.9.3</version>
</dependency>
```

### 2. Configuration into `application.yaml`
```yaml
spring:
  sendgrid:
      apiKey: ${SENDGRID_API_KEY:DUMMY-SENDGRID-API-KEY}
      fromEmail: ${FROM_EMAIL:DUMMY-EMAIL}
      templateId: ${TEMPLATE_ID:DUMMY-TEMPLATE-ID}
```

### 3. Configuration for sendgrid
```java
import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    @Bean
    public SendGrid sendGrid(@Value("${spring.sendgrid.apiKey}") String apiKey) {
        return new SendGrid(apiKey);
    }
}
```

### 4. Create service to send mail `EmailService.java`
```java
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailService {

    private final SendGrid sendGrid;

    @Value("${spring.sendgrid.fromEmail}")
    private String from;

    @Value("${spring.sendgrid.templateId}")
    private String templateId;

    /**
     * Send simple email with sendGrid
     *
     * @param to
     * @param subject
     * @param text
     */
    public void send(String to, String subject, String text) {
        Email fromEmail = new Email(from);
        Email toEmail = new Email(to);

        Content content = new Content("text/plain", text);
        Mail mail = new Mail(fromEmail, subject, toEmail, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            if (response.getStatusCode() == 202) {
                log.info("Email sent successfully");
            } else {
                log.error("Email sent failed");
            }
        } catch (IOException e) {
            log.error("Email sent failed, errorMessage={}", e.getMessage());
        }
    }

    public void sendEmailWithTemplate(String to, String name) throws IOException {
        Email fromEmail = new Email(from, "Nong Hoang Vu");
        Email toEmail = new Email(to);
        String subject = "Send Email With Template V1";

        String secretCode = UUID.randomUUID().toString();
        log.info("secretCode = {}", secretCode);

        // Create dynamic template data
        Map<String, String> dynamicTemplateData = new HashMap<>();
        dynamicTemplateData.put("name", name);
        dynamicTemplateData.put("my_url", "https://facebook.com/NongHoangVu04?secretCode=" + secretCode);

        Mail mail = new Mail();
        mail.setFrom(fromEmail);
        mail.setSubject(subject);
        Personalization personalization = new Personalization();
        personalization.addTo(toEmail);

        // Add dynamic template data
        dynamicTemplateData.forEach(personalization::addDynamicTemplateData);

        mail.addPersonalization(personalization);
        mail.setTemplateId(templateId); // Template ID from SendGrid

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sendGrid.api(request);
        if (response.getStatusCode() == 202) {
            log.info("Sent successfully");
        } else {
            log.error("Sent failed");
        }
    }
}
```

### 5. Using API to send mail `EmailController.java`
```java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vunh.service.EmailService;

@RestController
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-CONTROLLER")
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/send-email")
    public void sendEmail(@RequestParam String to, String subject, String body) {
        log.info("Sending email to {}", to);
        emailService.send(to, subject, body);
    }

    @PostMapping("/send-verification-email")
    public void sendVerificationEmail(@RequestParam String to, @RequestParam String name) {
        try {
            emailService.sendEmailWithTemplate(to, name);
            log.info("Email sent successfully!");
        } catch (Exception e) {
            log.info("Failed to send email.");
        }
    }
}
```