package tech.arhr.quingo.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import tech.arhr.quingo.dto.Visibility
import java.util.UUID

@Entity
@Table(name = "quizzes")
class QuizEntity {

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID()

    @Column(name = "owner_id", nullable = false)
    lateinit var ownerId: UUID

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "visibility", columnDefinition = "visibility", nullable = false)
    var visibility: Visibility = Visibility.PRIVATE

    @OneToOne(mappedBy = "quiz", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var draft: QuizDraftEntity? = null

    @OneToOne(mappedBy = "quiz", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var snapshot: QuizSnapshotEntity? = null
}
