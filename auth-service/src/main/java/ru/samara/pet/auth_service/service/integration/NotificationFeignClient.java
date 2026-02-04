package ru.samara.pet.auth_service.service.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${services.notification.url:http://localhost:8082}")
public interface NotificationFeignClient {

    @PostMapping(
            value = "/api/v1/accounts/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<String> sendEvent(@RequestBody String body);
}
