package tech.arhr.quingo.main_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.arhr.quingo.main_service.data.sql.entity.QuizEntity;
import tech.arhr.quingo.main_service.data.sql.repository.QuizRepository;
import tech.arhr.quingo.main_service.dto.QuizDto;
import tech.arhr.quingo.main_service.services.exceptions.EntityNotExistException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;

    public QuizDto createQuiz(QuizDto quizDto) {
        QuizEntity entity = quizRepository.save(QuizDto.toEntity(quizDto));
        return QuizDto.toDto(entity);
    }

    public QuizDto updateQuiz(UUID id, QuizDto quizDto) {
        Optional<QuizEntity> quizEntity = quizRepository.findById(id);
        if (quizEntity.isPresent()) {
            QuizEntity entity = quizEntity.get();
            entity.setName(quizDto.getName());
            entity.setDescription(quizDto.getDescription());
            quizRepository.save(entity);
            return quizDto.toDto(entity);
        } else{
            throw new EntityNotExistException();
        }
    }

    public QuizDto getById(UUID id) {
        Optional<QuizEntity> entity = quizRepository.findById(id);
        if (entity.isPresent()) {
            return QuizDto.toDto(entity.get());
        } else {
            throw new EntityNotExistException();
        }
    }

    public List<QuizDto> getAll() {
        List<QuizEntity> entities = quizRepository.findAll();
        return entities.stream().map(QuizDto::toDto).collect(Collectors.toList());
    }

    public void deleteAll(){
        quizRepository.deleteAll();
    }

    public void deleteQuiz(UUID id) {
        Optional<QuizEntity> optional = quizRepository.findById(id);
        if (optional.isPresent()) {
             quizRepository.delete(optional.get());
        } else {
            throw new EntityNotExistException();
        }
    }

}
