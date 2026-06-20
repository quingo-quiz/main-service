package tech.arhr.quingo.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "quiz_drafts")
class QuizDraftEntity {

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID()

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false, unique = true)
    lateinit var quiz: QuizEntity

    @Column(name = "title")
    var title: String? = null

    @Column(name = "description")
    var description: String? = null

    @OneToMany(mappedBy = "draft", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("position ASC")
    var cards: MutableList<CardEntity> = mutableListOf()

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @Column(name = "modified_at", nullable = false)
    lateinit var modifiedAt: Instant
}
