package tech.arhr.quingo.persistence.entity

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.hibernate.orm.JsonFormat

data class OptionJson(
    val id: Int,
    val text: String,
    @field:JsonProperty("correct")
    @get:JsonProperty("correct")
    val isCorrect: Boolean,
)
