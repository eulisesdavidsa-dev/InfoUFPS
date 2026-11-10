package org.infoUFPS.Controllers.User;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
import org.infoUFPS.Security.ActorContext;
import org.infoUFPS.Security.CurrentUser;

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
    ActorContext actorContext;

    public record CreateReviewRequest(
            @NotBlank String tipoDestino,
            String profesorId,
            String materiaId,
            @Min(1) @Max(5) int calificacion,
            @NotBlank @Size(max = 1000) String comentario,
            @Size(max = 150) String materiaCursada
    ) {}

    public record UpdateReviewRequest(
            @Min(1) @Max(5) int calificacion,
            @NotBlank @Size(max = 1000) String comentario,
            @Size(max = 150) String materiaCursada
    ) {}

    @POST
    public Response create(@Valid CreateReviewRequest request) {
        String userId = actorContext.currentUser().requireAccountId();
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
        String userId = actorContext.currentUser().requireAccountId();
        List<ResenaDto> list = getUserResenasQuery.execute(userId);
        return Response.ok(list).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, @Valid UpdateReviewRequest request) {
        String userId = actorContext.currentUser().requireAccountId();
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
        CurrentUser user = actorContext.currentUser();
        String userId = user.requireAccountId();
        boolean isAdmin = user.admin();
        deleteResenaHandler.execute(id, userId, isAdmin);
        return Response.noContent().build();
    }
}

