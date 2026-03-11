package ru.samara.pet.auth_service.service.temp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {

    @Autowired
    private EmailSender emailSender;


    @Async("ioExecutor")
    public CompletableFuture<Void> sendNotification(String email) {
        emailSender.sendEmail(email, "Notification sent!");
        return CompletableFuture.completedFuture(null);
    }
}
