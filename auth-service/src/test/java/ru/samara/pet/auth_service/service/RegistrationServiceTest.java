package ru.samara.pet.auth_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.samara.pet.auth_service.service.temp.NotificationService;
import ru.samara.pet.auth_service.service.temp.RegistrationService;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void register_ShouldCallNotificationService() {
        // Arrange
        String email = "test@example.com";
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        when(notificationService.sendNotification(email)).thenReturn(future);

        // Act
        registrationService.register(email);

        // Assert
        verify(notificationService, times(1)).sendNotification(email);
    }

    @Test
    void register_WhenNotificationServiceThrowsException_ShouldHandleException() {
        // Arrange
        String email = "test@example.com";
        CompletableFuture<Void> future = CompletableFuture.failedFuture(new RuntimeException("Test exception"));
        when(notificationService.sendNotification(email)).thenReturn(future);

        // Act
        registrationService.register(email);

        // Assert
        verify(notificationService, times(1)).sendNotification(email);
    }
}
