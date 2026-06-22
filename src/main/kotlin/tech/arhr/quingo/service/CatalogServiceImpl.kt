package tech.arhr.quingo.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tech.arhr.quingo.dto.pagination.PageDto
import tech.arhr.quingo.dto.catalog.CatalogItemDto
import tech.arhr.quingo.dto.quiz.Visibility
import tech.arhr.quingo.exceptions.EntityNotFoundException
import tech.arhr.quingo.persistence.entity.QuizEntity
import tech.arhr.quingo.persistence.repository.QuizRepository
import java.util.UUID

@ApplicationScoped
@Transactional
class CatalogServiceImpl(
    private val quizRepository: QuizRepository,
) : CatalogService {

    override fun search(query: String?, page: Int?, size: Int?): PageDto<CatalogItemDto> {
        val pageIndex = (page ?: 0).coerceAtLeast(0)
        val pageSize = (size ?: 20).coerceIn(1, 100)

        val total = quizRepository.countCatalogItems(query)
        val items = quizRepository.searchCatalog(query, pageIndex, pageSize).map { it.toCatalogItem() }
        val totalPages = ((total + pageSize - 1) / pageSize).toInt()

        return PageDto(items, pageIndex, pageSize, total, totalPages)
    }

    private fun QuizEntity.toCatalogItem(): CatalogItemDto {
        val snapshot = snapshot!!
        return CatalogItemDto(
            id = id,
            title = snapshot.title,
            description = snapshot.description,
            cardCount = snapshot.cards.size,
            createdAt = snapshot.createdAt,
            modifiedAt = snapshot.modifiedAt,
            ownerId = ownerId,
        )
    }

    override fun getById(id: UUID): CatalogItemDto {
        val quiz = quizRepository.findById(id)
        if (quiz == null || quiz.visibility != Visibility.PUBLIC || quiz.snapshot == null) {
            throw EntityNotFoundException("Quiz")
        }
        return quiz.toCatalogItem()
    }
}
