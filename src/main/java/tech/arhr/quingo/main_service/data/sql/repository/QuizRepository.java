package tech.arhr.quingo.main_service.data.sql.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.arhr.quingo.main_service.data.sql.entity.QuizEntity;

import java.util.UUID;

public interface QuizRepository extends JpaRepository<QuizEntity, UUID> {
}
