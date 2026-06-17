package tech.arhr.quingo

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/ping")
class   PingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun ping() = "main-service is up"
}
