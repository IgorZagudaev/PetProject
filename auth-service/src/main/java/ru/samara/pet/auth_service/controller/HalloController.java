package ru.samara.pet.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.samara.pet.auth_service.service.temp.RegistrationService;

@RestController
@RequestMapping("/hallo")
public class HalloController {

    RegistrationService registrationService;
    public HalloController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/index")
    public ResponseEntity<?> showHallo(){
        registrationService.register("test@test.com");
        return ResponseEntity.ok("Hallo");
    }
}
