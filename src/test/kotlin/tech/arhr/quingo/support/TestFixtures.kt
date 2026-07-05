package tech.arhr.quingo.support

/**
 * Билдеры JSON-тел запросов для интеграционных тестов.
 */
object TestFixtures {

    fun createQuizBody(title: String = "My quiz", visibility: String = "PUBLIC", description: String? = null): String {
        val desc = description?.let { "\"$it\"" } ?: "null"
        return """{"title":"$title","description":$desc,"visibility":"$visibility"}"""
    }

    fun updateVisibilityBody(visibility: String): String =
        """{"visibility":"$visibility"}"""

    /** Валидный черновик с одной карточкой каждого типа — проходит строгую валидацию публикации. */
    fun validDraftBody(title: String = "My quiz"): String =
        """
        {
          "title": "$title",
          "description": "desc",
          "cards": [
            $SINGLE_CHOICE_CARD,
            $MULTIPLE_CHOICE_CARD,
            $TEXT_INPUT_CARD
          ]
        }
        """.trimIndent()

    /** Черновик с одной пустой карточкой — не проходит валидацию публикации. */
    fun incompleteDraftBody(title: String = "My quiz"): String =
        """{"title":"$title","cards":[{"type":"SINGLE_CHOICE"}]}"""

    fun draftBody(title: String?, cardsJson: String): String {
        val t = title?.let { "\"$it\"" } ?: "null"
        return """{"title":$t,"cards":[$cardsJson]}"""
    }

    const val SINGLE_CHOICE_CARD =
        """{"type":"SINGLE_CHOICE","questionText":"Q1","timerSeconds":30,"options":[{"text":"A","isCorrect":true},{"text":"B","isCorrect":false}]}"""

    const val MULTIPLE_CHOICE_CARD =
        """{"type":"MULTIPLE_CHOICE","questionText":"Q2","timerSeconds":20,"options":[{"text":"A","isCorrect":true},{"text":"B","isCorrect":true}]}"""

    const val TEXT_INPUT_CARD =
        """{"type":"TEXT_INPUT","questionText":"Q3","timerSeconds":15,"acceptedTexts":["yes","да"]}"""
}
