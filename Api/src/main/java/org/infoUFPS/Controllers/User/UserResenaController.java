package org.infoUFPS.Controllers.User;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.DTOs.Requests.CreateResenaCommand;
import org.infoUFPS.DTOs.Requests.UpdateResenaCommand;
import org.infoUFPS.DTOs.ResenaDto;
import org.infoUFPS.Enums.TipoDestino;
import org.infoUFPS.Features.Resena.Commands.CreateResenaHandler;
import org.infoUFPS.Features.Resena.Commands.DeleteResenaHandler;
import org.infoUFPS.Features.Resena.Commands.UpdateResenaHandler;
import org.infoUFPS.Features.Resena.Queries.GetUserResenasQuery;
import org.infoUFPS.Security.AuthContext;

import java.util.List;

@Path("/api/v1/user/resenas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResenaController {

    @Inject
    CreateResenaHandler createResenaHandler;

    @Inject
    UpdateResenaHandler updateResenaHandler;

    @Inject
    DeleteResenaHandler deleteResenaHandler;

    @Inject
    GetUserResenasQuery getUserResenasQuery;

    @Inject
    AuthContext authContext;

    public record CreateReviewRequest(
            String tipoDestino,
            String profesorId,
            String materiaId,
            int calificacion,
            String comentario,
            String materiaCursada
    ) {}

    public record UpdateReviewRequest(
            int calificacion,
            String comentario,
            String materiaCursada
    ) {}

    @POST
    public Response create(CreateReviewRequest request) {
        String userId = authContext.requireAuthenticatedUserId();
        TipoDestino destino = (request.tipoDestino() != null)
                ? TipoDestino.fromString(request.tipoDestino())
                : null;

        CreateResenaCommand command = new CreateResenaCommand(
                userId,
                destino,
                request.profesorId(),
                request.materiaId(),
                request.calificacion(),
                request.comentario(),
                request.materiaCursada()
        );

        ResenaDto created = createResenaHandler.execute(command);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    public Response getMyReviews() {
        String userId = authContext.requireAuthenticatedUserId();
        List<ResenaDto> list = getUserResenasQuery.execute(userId);
        return Response.ok(list).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, UpdateReviewRequest request) {
        String userId = authContext.requireAuthenticatedUserId();
        UpdateResenaCommand command = new UpdateResenaCommand(
                id,
                userId,
                request.calificacion(),
                request.comentario(),
                request.materiaCursada()
        );

        ResenaDto updated = updateResenaHandler.execute(command);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        String userId = authContext.requireAuthenticatedUserId();
        boolean isAdmin = authContext.isAdmin();
        deleteResenaHandler.execute(id, userId, isAdmin);
        return Response.noContent().build();
    }
}

