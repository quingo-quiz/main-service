package tech.arhr.quingo.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import tech.arhr.quingo.dto.CardType
import java.util.UUID

@Entity
@Table(name = "cards")
class CardEntity {

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id")
    var draft: QuizDraftEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id")
    var snapshot: QuizSnapshotEntity? = null

    @Column(name = "position", nullable = false)
    var position: Int = 0

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", columnDefinition = "card_type", nullable = false)
    lateinit var type: CardType

    @Column(name = "question_text", nullable = false)
    lateinit var questionText: String

    @Column(name = "timer_seconds", nullable = false)
    var timerSeconds: Int = 0

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options")
    var options: MutableList<OptionJson>? = null

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "accepted_texts", columnDefinition = "text[]")
    var acceptedTexts: MutableList<String>? = null
}
