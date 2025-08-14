package com.fitness.fitnessapi.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;


    public void sendOtpEmail(String to, String subject, String body) {
        System.out.println("inside the send otp");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("movemate1111@gmail.com"); // Sender's email
        message.setTo(to); // Receiver's email
        message.setSubject(subject); // Email subject
        message.setText(body); // Email body

        mailSender.send(message); // Send email

    }

    @PostConstruct
    public void testEmail() {
        try {
            sendOtpEmail("tilakmoradiya1111@gmail.com", "Test OTP", "Your OTP is 1234");
            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
