package ru.samara.pet.auth_service.service.temp;

import org.springframework.stereotype.Service;

@Service
public class EmailSender {

    public void sendEmail(String email, String message) {
        System.out.println("Sending email to " + email + " with message: " + message);
        throw new RuntimeException("Error sending email");
    }
}
