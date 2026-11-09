package org.infoUFPS.Controllers.Public;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.DTOs.GlobalSearchResultDto;
import org.infoUFPS.Features.Search.Queries.GlobalSearchQuery;

@Path("/api/v1/public/busqueda-global")
@Produces(MediaType.APPLICATION_JSON)
public class GlobalSearchController {

    @Inject
    GlobalSearchQuery globalSearchQuery;

    @GET
    public Response search(@QueryParam("q") String query) {
        GlobalSearchResultDto result = globalSearchQuery.execute(query);
        return Response.ok(result).build();
    }
}

