package tech.arhr.quingo.persistence.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import tech.arhr.quingo.dto.quiz.Visibility
import tech.arhr.quingo.persistence.entity.QuizEntity
import java.util.UUID

@ApplicationScoped
class QuizRepository : PanacheRepositoryBase<QuizEntity, UUID> {

    fun findByOwner(ownerId: UUID): List<QuizEntity> =
        list("ownerId", ownerId)

    fun searchCatalog(query: String?, page: Int, size: Int): List<QuizEntity> {
        val (filter, params) = catalogFilter(query)
        val sort = Sort.by("snapshot.modifiedAt", Sort.Direction.Descending)
        return find(filter, sort, *params).page(page, size).list()
    }

    fun countCatalogItems(query: String?): Long {
        val (filter, params) = catalogFilter(query)
        return count(filter, *params)
    }

    private fun catalogFilter(query: String?): Pair<String, Array<Any>> {
        val term = query?.trim()
        if (term.isNullOrEmpty()) {
            return "visibility = ?1 and snapshot is not null" to arrayOf(Visibility.PUBLIC)
        }
        return "visibility = ?1 and snapshot is not null and " +
                "(lower(snapshot.title) like ?2 or lower(snapshot.description) like ?2)" to
                arrayOf(Visibility.PUBLIC, "%${term.lowercase()}%")
    }
}
