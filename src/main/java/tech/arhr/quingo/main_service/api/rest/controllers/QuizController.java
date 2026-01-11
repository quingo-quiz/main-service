package tech.arhr.quingo.main_service.api.rest.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.arhr.quingo.main_service.dto.QuizDto;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/quizzes")
public class QuizController {
    @PostMapping()
    public ResponseEntity<?> createQuiz(QuizDto quizDto) {
        Integer id = 1;
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    public QuizDto getQuiz(@PathVariable UUID id) {
        return null;
    }

    @GetMapping("/all")
    public List<QuizDto> getQuizzes() {
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteQuiz(@PathVariable UUID id) {}

}
