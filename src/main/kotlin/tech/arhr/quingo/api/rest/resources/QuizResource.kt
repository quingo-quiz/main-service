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
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestResponse
import tech.arhr.quingo.api.rest.mappers.QuizMapperImpl
import tech.arhr.quingo.api.rest.models.SuccessResponse
import tech.arhr.quingo.api.rest.models.quiz.CreateQuizRequest
import tech.arhr.quingo.api.rest.models.quiz.Quiz
import tech.arhr.quingo.api.rest.models.quiz.QuizSummary
import tech.arhr.quingo.api.rest.models.quiz.SaveDraftRequest
import tech.arhr.quingo.api.rest.models.quiz.UpdateQuizRequest
import tech.arhr.quingo.api.security.CurrentUser
import tech.arhr.quingo.service.QuizService
import java.time.Instant
import java.util.UUID

@Path("/quizzes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class QuizResource(
    private val currentUser: CurrentUser,
    private val quizService: QuizService,
) {

    @GET
    fun list(): RestResponse<SuccessResponse<List<QuizSummary>>> {
        val data = quizService.listSummaries(currentUser.id).map { QuizMapperImpl.toApi(it) }
        return ok(data)
    }

    @POST
    fun create(@Valid request: CreateQuizRequest): RestResponse<SuccessResponse<Quiz>> {
        val quiz = quizService.create(currentUser.id, QuizMapperImpl.toDto(request))
        return created(QuizMapperImpl.toApi(quiz))
    }

    @GET
    @Path("/{id}")
    fun get(@PathParam("id") id: UUID): RestResponse<SuccessResponse<Quiz>> {
        val quiz = quizService.get(currentUser.id, id)
        return ok(QuizMapperImpl.toApi(quiz))
    }

    @PATCH
    @Path("/{id}")
    fun update(@PathParam("id") id: UUID, @Valid request: UpdateQuizRequest): RestResponse<SuccessResponse<Quiz>> {
        val quiz = quizService.changeVisibility(currentUser.id, id, QuizMapperImpl.toDomain(request.visibility))
        return ok(QuizMapperImpl.toApi(quiz))
    }

    @DELETE
    @Path("/{id}")
    fun remove(@PathParam("id") id: UUID): RestResponse<SuccessResponse<Unit>> {
        quizService.delete(currentUser.id, id)
        return empty()
    }

    @POST
    @Path("/{id}/draft")
    fun startEditing(@PathParam("id") id: UUID): RestResponse<SuccessResponse<Quiz>> {
        val quiz = quizService.startEditing(currentUser.id, id)
        return created(QuizMapperImpl.toApi(quiz))
    }

    @PUT
    @Path("/{id}/draft")
    fun saveDraft(@PathParam("id") id: UUID, @Valid request: SaveDraftRequest): RestResponse<SuccessResponse<Quiz>> {
        val quiz = quizService.saveDraft(currentUser.id, id, QuizMapperImpl.toDto(request))
        return ok(QuizMapperImpl.toApi(quiz))
    }

    @POST
    @Path("/{id}/publish")
    fun publish(@PathParam("id") id: UUID): RestResponse<SuccessResponse<Quiz>> {
        val quiz = quizService.publish(currentUser.id, id)
        return ok(QuizMapperImpl.toApi(quiz))
    }

    @DELETE
    @Path("/{id}/draft")
    fun discardDraft(@PathParam("id") id: UUID): RestResponse<SuccessResponse<Unit>> {
        quizService.discardDraft(currentUser.id, id)
        return empty()
    }

    private fun <T> ok(data: T): RestResponse<SuccessResponse<T>> = build(Response.Status.OK, data)

    private fun <T> created(data: T): RestResponse<SuccessResponse<T>> = build(Response.Status.CREATED, data)

    private fun empty(): RestResponse<SuccessResponse<Unit>> = build(Response.Status.OK, null)

    private fun <T> build(status: Response.Status, data: T?): RestResponse<SuccessResponse<T>> {
        val body = SuccessResponse.of(status, data, Instant.now())
        return RestResponse.ResponseBuilder.create<SuccessResponse<T>>(status.statusCode).entity(body).build()
    }
}
