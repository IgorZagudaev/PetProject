package ru.samara.pet.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hallo")
public class HalloController {

    @GetMapping("/index")
    public ResponseEntity<?> showHallo(){
        return ResponseEntity.ok("Hallo");
    }
}
