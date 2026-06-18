package tech.arhr.quingo.persistence.mapper

import io.mcarle.konvert.api.Konverter
import tech.arhr.quingo.dto.CardDto
import tech.arhr.quingo.dto.CardOptionDto
import tech.arhr.quingo.dto.QuizContentDto
import tech.arhr.quingo.persistence.entity.CardEntity
import tech.arhr.quingo.persistence.entity.OptionJson
import tech.arhr.quingo.persistence.entity.QuizDraftEntity
import tech.arhr.quingo.persistence.entity.QuizSnapshotEntity

@Konverter
interface QuizEntityMapper {

    fun toDto(option: OptionJson): CardOptionDto

    fun toDto(card: CardEntity): CardDto

    fun toContent(draft: QuizDraftEntity): QuizContentDto

    fun toContent(snapshot: QuizSnapshotEntity): QuizContentDto
}
