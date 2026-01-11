package tech.arhr.quingo.main_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class QuizDto {
    private UUID id;

    @NotNull
    @Size(min = 2, max = 80)
    private String name;

    @Size(min = 10, max = 700)
    private String description;
}
