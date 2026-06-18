package tech.arhr.quingo.persistence.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import tech.arhr.quingo.persistence.entity.QuizEntity
import java.util.UUID

@ApplicationScoped
class QuizRepository : PanacheRepositoryBase<QuizEntity, UUID> {

    fun findByOwner(ownerId: UUID): List<QuizEntity> =
        list("ownerId", ownerId)

    fun findByIdAndOwner(id: UUID, ownerId: UUID): QuizEntity? =
        find("id = ?1 and ownerId = ?2", id, ownerId).firstResult()
}
