package com.fitness.fitnessapi.service;//package com.fitness.fitnessapi.service;
//
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//
//    public void sendOtpEmail(String to, String subject, String body) {
//        System.out.println("inside the send otp");
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("movemate1111@gmail.com"); // Sender's email
//        message.setTo(to); // Receiver's email
//        message.setSubject(subject); // Email subject
//        message.setText(body); // Email body
//
//        mailSender.send(message); // Send email
//
//    }
//
//}

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String subject, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            // HTML template for OTP email
            String htmlContent = """
                    <html>
                        <body style="font-family: Arial, sans-serif;">
                            <h2 style="color: #4CAF50;">Your OTP Code</h2>
                            <p>Hello,</p>
                            <p>Your One-Time Password (OTP) is:</p>
                            <h1 style="color: #FF5722;">%s</h1>
                            <p>This OTP is valid for <b>5 minutes</b>. Please do not share it with anyone.</p>
                            <br>
                            <p>Best regards,<br>Fitness Partner Team</p>
                        </body>
                    </html>
                    """.formatted(otp);

            helper.setText(htmlContent, true); // true = HTML format

            mailSender.send(message);

            System.out.println("✅ OTP email sent successfully to " + to);

        } catch (Exception e) {
            System.err.println("❌ Failed to send OTP email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
