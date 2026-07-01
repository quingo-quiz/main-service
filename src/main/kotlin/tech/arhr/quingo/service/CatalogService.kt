package tech.arhr.quingo.service

import tech.arhr.quingo.dto.catalog.CatalogItemDto
import tech.arhr.quingo.dto.catalog.CatalogPageDto
import java.util.UUID

interface CatalogService {
    fun search(query: String?, page: Int? = 0, size: Int? = 20): CatalogPageDto
    fun getById(id: UUID): CatalogItemDto
}
