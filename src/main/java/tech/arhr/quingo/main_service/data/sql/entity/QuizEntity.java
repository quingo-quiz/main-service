package tech.arhr.quingo.main_service.data.sql.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quizzes")
public class QuizEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Size(min = 1, max = 100)
    private String name;

    @Column
    @Size(min = 10, max = 700)
    private String description;
}
