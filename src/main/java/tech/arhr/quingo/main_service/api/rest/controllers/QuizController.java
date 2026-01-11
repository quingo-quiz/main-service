package tech.arhr.quingo.main_service.api.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.arhr.quingo.main_service.dto.QuizDto;
import tech.arhr.quingo.main_service.services.QuizService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @PostMapping()
    public ResponseEntity<?> createQuiz(@Valid @RequestBody QuizDto quiz) {
        QuizDto quizDto = quizService.createQuiz(quiz);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(quizDto.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public QuizDto getQuiz(@PathVariable UUID id) {
        return quizService.getById(id);
    }

    @PutMapping("/{id}")
    public QuizDto updateQuiz(@PathVariable UUID id, @Valid @RequestBody QuizDto quiz) {
        return quizService.updateQuiz(id, quiz);
    }

    @GetMapping("/all")
    public List<QuizDto> getQuizzes() {
        return quizService.getAll();
    }

    @DeleteMapping("/all")
    public void deleteQuizzes() {
        quizService.deleteAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable UUID id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

}
