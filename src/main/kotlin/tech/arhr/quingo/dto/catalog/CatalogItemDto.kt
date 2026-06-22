package tech.arhr.quingo.dto.catalog

import java.time.Instant
import java.util.*

data class CatalogItemDto (
    val id: UUID,
    val title: String,
    val description: String?,
    val cardCount: Int,
    val createdAt: Instant,
    val modifiedAt: Instant,
    val ownerId: UUID,
)