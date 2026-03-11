package ru.samara.pet.auth_service.service.temp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    @Autowired
    private NotificationService notificationService;

    public void register(String email) {
        notificationService.sendNotification(email).exceptionally(
                exception -> {
                    System.out.println("Error sending notification: " + exception.getMessage());
                    return null;
                }
        );
    }
}
