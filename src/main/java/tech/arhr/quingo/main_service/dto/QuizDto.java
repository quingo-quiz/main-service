package tech.arhr.quingo.main_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.arhr.quingo.main_service.data.sql.entity.QuizEntity;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDto {
    private UUID id;

    @NotNull
    @Size(min = 2, max = 80)
    private String name;

    @Size(min = 10, max = 700)
    private String description;



    public static QuizDto toDto(QuizEntity quizEntity) {
        return QuizDto.builder()
                .id(quizEntity.getId())
                .name(quizEntity.getName())
                .description(quizEntity.getDescription())
                .build();
    }

    public static QuizEntity toEntity(QuizDto quizDto) {
        return QuizEntity.builder()
                .id(quizDto.getId())
                .name(quizDto.getName())
                .description(quizDto.getDescription())
                .build();
    }
}
