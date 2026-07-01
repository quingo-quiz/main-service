package tech.arhr.quingo.dto.catalog

data class CatalogPageDto(
    val items: List<CatalogItemDto>,
    val page: Int,
    val size: Int,
    val total: Long,
    val totalPages: Int,
)
