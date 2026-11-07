package org.infoUFPS.Controllers.Public;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.Features.Content.Queries.GetModuleQuery;

@Path("/api/v1/public/catalog")
@Produces(MediaType.APPLICATION_JSON)
public class ModuleController {

    @Inject
    GetModuleQuery getPublicCatalog;

    @GET
    public Response getCatalog() {
        return Response.ok(getPublicCatalog.execute())
                .header("Cache-Control", "public, max-age=60")
                .build();
    }
}
