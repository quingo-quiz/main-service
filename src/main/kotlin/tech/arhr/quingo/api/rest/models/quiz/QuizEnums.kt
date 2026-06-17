package tech.arhr.quingo.api.rest.models.quiz

enum class Visibility {
    PUBLIC,
    PRIVATE,
}

/**
 * Публиковался ли квиз. */
enum class QuizStatus {
    UNPUBLISHED,
    PUBLISHED,
}

enum class CardType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    TEXT_INPUT,
}

/** Какую версию квиза представляет сводка [QuizSummary]. */
enum class QuizVersion {
    DRAFT,
    PUBLISHED,
}
