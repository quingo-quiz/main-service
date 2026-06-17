package tech.arhr.quingo.api.rest.resources

import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.PATCH
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.RestResponse
import tech.arhr.quingo.api.rest.models.SuccessResponse
import tech.arhr.quingo.api.rest.models.quiz.CreateQuizRequest
import tech.arhr.quingo.api.rest.models.quiz.Quiz
import tech.arhr.quingo.api.rest.models.quiz.QuizSummary
import tech.arhr.quingo.api.rest.models.quiz.SaveDraftRequest
import tech.arhr.quingo.api.rest.models.quiz.UpdateQuizRequest
import tech.arhr.quingo.api.security.CurrentUser
import java.util.UUID

/** REST-слой управления квизами. Владелец берётся из [CurrentUser]. */
@Path("/quizzes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class QuizResource(private val currentUser: CurrentUser) {

    /** Список квизов владельца. */
    @GET
    fun list(): RestResponse<SuccessResponse<List<QuizSummary>>> =
        TODO()

    /** Создать квиз. */
    @POST
    fun create(@Valid request: CreateQuizRequest): RestResponse<SuccessResponse<Quiz>> =
        TODO()

    /** Получить квиз целиком. */
    @GET
    @Path("/{id}")
    fun get(@PathParam("id") id: UUID): RestResponse<SuccessResponse<Quiz>> =
        TODO()

    /** Изменить видимость квиза. */
    @PATCH
    @Path("/{id}")
    fun update(@PathParam("id") id: UUID, @Valid request: UpdateQuizRequest): RestResponse<SuccessResponse<Quiz>> =
        TODO()

    /** Удалить квиз. */
    @DELETE
    @Path("/{id}")
    fun remove(@PathParam("id") id: UUID): RestResponse<SuccessResponse<Unit>> =
        TODO()

    /** Создать черновик для опубликованного квиза. */
    @POST
    @Path("/{id}/draft")
    fun startEditing(@PathParam("id") id: UUID): RestResponse<SuccessResponse<Quiz>> =
        TODO()

    /** Сохранить черновик. */
    @PUT
    @Path("/{id}/draft")
    fun saveDraft(@PathParam("id") id: UUID, @Valid request: SaveDraftRequest): RestResponse<SuccessResponse<Quiz>> =
        TODO()

    /** Опубликовать черновик. */
    @POST
    @Path("/{id}/publish")
    fun publish(@PathParam("id") id: UUID): RestResponse<SuccessResponse<Quiz>> =
        TODO()

    /** Удалить черновик. */
    @DELETE
    @Path("/{id}/draft")
    fun discardDraft(@PathParam("id") id: UUID): RestResponse<SuccessResponse<Unit>> =
        TODO()
}
