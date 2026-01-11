package tech.arhr.quingo.main_service.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
public class QuizEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    @Size(min = 1, max = 100)
    private String name;

    @Column
    @Size(min = 10, max = 700)
    private String description;
}
