package tech.arhr.quingo.dto.pagination

data class PageDto<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val total: Long,
    val totalPages: Int,
)