package org.infoUFPS.Controllers.User;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.DTOs.ProfesorDetailDto;
import org.infoUFPS.DTOs.Requests.ProposeProfesorCommand;
import org.infoUFPS.DTOs.Requests.UpdateProposedProfesorCommand;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Features.Profesor.Commands.ProposeProfesorHandler;
import org.infoUFPS.Features.Profesor.Commands.UpdateProposedProfesorHandler;
import org.infoUFPS.Features.Profesor.Queries.GetProfesorPropuestasQuery;
import org.infoUFPS.Security.ActorContext;

import java.util.List;

@Path("/api/v1/user/profesores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserProfesorController {

    @Inject
    ProposeProfesorHandler proposeProfesorHandler;

    @Inject
    UpdateProposedProfesorHandler updateProposedProfesorHandler;

    @Inject
    GetProfesorPropuestasQuery getProfesorPropuestasQuery;

    @Inject
    ActorContext actorContext;

    public record ProposeRequest(
            @NotBlank @Size(max = 150) String nombre,
            String facultad,
            @Email @Size(max = 150) String email) {}

    public record UpdateProposalRequest(
            @NotBlank @Size(max = 150) String nombre,
            String facultad,
            @Email @Size(max = 150) String email) {}

    @POST
    public Response propose(@Valid ProposeRequest request) {
        String userId = actorContext.currentUser().requireAccountId();
        Facultad facultad = (request.facultad() != null && !request.facultad().isBlank())
                ? Facultad.fromString(request.facultad())
                : Facultad.NO_ESPECIFICADA;

        ProposeProfesorCommand command = new ProposeProfesorCommand(
                request.nombre(),
                facultad,
                request.email(),
                userId
        );

        ProfesorDetailDto created = proposeProfesorHandler.execute(command);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/propuestas")
    public Response getMyProposals() {
        String userId = actorContext.currentUser().requireAccountId();
        List<ProfesorDetailDto> proposals = getProfesorPropuestasQuery.execute(userId);
        return Response.ok(proposals).build();
    }

    @PUT
    @Path("/propuestas/{id}")
    public Response updateProposal(@PathParam("id") String id, @Valid UpdateProposalRequest request) {
        String userId = actorContext.currentUser().requireAccountId();
        Facultad facultad = (request.facultad() != null && !request.facultad().isBlank())
                ? Facultad.fromString(request.facultad())
                : null;

        UpdateProposedProfesorCommand command = new UpdateProposedProfesorCommand(
                id,
                request.nombre(),
                facultad,
                request.email(),
                userId
        );

        ProfesorDetailDto updated = updateProposedProfesorHandler.execute(command);
        return Response.ok(updated).build();
    }
}

