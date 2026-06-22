package tech.arhr.quingo.service

import tech.arhr.quingo.dto.pagination.PageDto
import tech.arhr.quingo.dto.catalog.CatalogItemDto
import java.util.UUID

interface CatalogService {
    fun search(query: String?, page: Int? = 0, size: Int? = 20): PageDto<CatalogItemDto>
    fun getById(id: UUID): CatalogItemDto
}
