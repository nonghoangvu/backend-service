/*
 * Author: Nong Hoang Vu || JavaTech
 * Facebook:https://facebook.com/NongHoangVu04
 * Github: https://github.com/JavaTech04
 * Youtube: https://www.youtube.com/@javatech04/?sub_confirmation=1
 */
package vn.vunh.service;

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
import org.springframework.kafka.annotation.KafkaListener;
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

    @KafkaListener(topics = "send-email", groupId = "send-mail-groups")
    public void sendEmailWithTemplateKafka(String message) throws IOException {
        String[] arr = message.split(",");
        String to = arr[0].substring(arr[0].indexOf('=') + 1);
        String name = arr[1].substring(arr[1].indexOf('=') + 1);

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