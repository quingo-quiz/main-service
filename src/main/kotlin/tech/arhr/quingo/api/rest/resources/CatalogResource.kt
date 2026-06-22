package tech.arhr.quingo.api.rest.resources

import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestResponse
import tech.arhr.quingo.api.rest.models.SuccessResponse
import tech.arhr.quingo.dto.pagination.PageDto
import tech.arhr.quingo.dto.catalog.CatalogItemDto
import tech.arhr.quingo.service.CatalogService
import java.time.Instant
import java.util.UUID

@Path("/catalog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class CatalogResource(
    private val catalogService: CatalogService,
) {
    @GET
    fun getAll(
        @QueryParam("q") query: String?,
        @QueryParam("page") page: Int?,
        @QueryParam("size") size: Int?,
    ): RestResponse<SuccessResponse<PageDto<CatalogItemDto>>> {
        return ok(catalogService.search(query, page, size))
    }

    @GET
    @Path("/{id}")
    fun getById(
        @PathParam("id") id: UUID
    ): RestResponse<SuccessResponse<CatalogItemDto>> {
        return ok(catalogService.getById(id))
    }

    private fun <T> ok(data: T): RestResponse<SuccessResponse<T>> = build(Response.Status.OK, data)

    private fun <T> build(status: Response.Status, data: T?): RestResponse<SuccessResponse<T>> {
        val body = SuccessResponse.of(status, data, Instant.now())
        return RestResponse.ResponseBuilder.create<SuccessResponse<T>>(status.statusCode).entity(body).build()
    }
}