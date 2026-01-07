package tech.arhr.quingo.main_service.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok().build();
    }
}
