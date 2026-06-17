package tech.arhr.quingo.dto

data class CreateQuizDto(
    val title: String,
    val description: String? = null,
    val visibility: Visibility,
)

data class OptionDraftDto(
    val text: String,
    val isCorrect: Boolean,
)

data class CardDraftDto(
    val type: CardType,
    val questionText: String,
    val timerSeconds: Int,
    val options: List<OptionDraftDto>? = null,
    val acceptedTexts: List<String>? = null,
)

data class DraftContentDto(
    val title: String,
    val description: String? = null,
    val cards: List<CardDraftDto>,
)
