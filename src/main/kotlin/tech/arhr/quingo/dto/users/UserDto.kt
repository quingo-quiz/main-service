package tech.arhr.quingo.dto.users

import java.util.UUID

data class UserDto(
    val id: UUID,
    val username: String,
)