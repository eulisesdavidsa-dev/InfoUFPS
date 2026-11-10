package org.infoUFPS.Controllers.User;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.DTOs.MateriaDetailDto;
import org.infoUFPS.DTOs.Requests.ProposeMateriaCommand;
import org.infoUFPS.DTOs.Requests.UpdateProposedMateriaCommand;
import org.infoUFPS.Enums.TipoMateria;
import org.infoUFPS.Features.Materia.Commands.ProposeMateriaHandler;
import org.infoUFPS.Features.Materia.Commands.UpdateProposedMateriaHandler;
import org.infoUFPS.Features.Materia.Queries.GetMateriaPropuestasQuery;
import org.infoUFPS.Security.ActorContext;

import java.util.List;

@Path("/api/v1/user/materias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserMateriaController {

    @Inject
    ProposeMateriaHandler proposeMateriaHandler;

    @Inject
    UpdateProposedMateriaHandler updateProposedMateriaHandler;

    @Inject
    GetMateriaPropuestasQuery getMateriaPropuestasQuery;

    @Inject
    ActorContext actorContext;

    public record ProposeMateriaRequest(
            @NotBlank @Size(max = 150) String nombre,
            @Size(max = 30) String codigo,
            @Min(1) Integer creditos,
            String tipo) {}

    public record UpdateMateriaProposalRequest(
            @NotBlank @Size(max = 150) String nombre,
            @Size(max = 30) String codigo,
            @Min(1) Integer creditos,
            String tipo) {}

    @POST
    public Response propose(@Valid ProposeMateriaRequest request) {
        String userId = actorContext.currentUser().requireAccountId();
        TipoMateria tipo = (request.tipo() != null && !request.tipo().isBlank())
                ? TipoMateria.fromString(request.tipo())
                : null;

        ProposeMateriaCommand command = new ProposeMateriaCommand(
                request.nombre(),
                request.codigo(),
                request.creditos(),
                tipo,
                userId
        );

        MateriaDetailDto created = proposeMateriaHandler.execute(command);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/propuestas")
    public Response getMyProposals() {
        String userId = actorContext.currentUser().requireAccountId();
        List<MateriaDetailDto> proposals = getMateriaPropuestasQuery.execute(userId);
        return Response.ok(proposals).build();
    }

    @PUT
    @Path("/propuestas/{id}")
    public Response updateProposal(@PathParam("id") String id, @Valid UpdateMateriaProposalRequest request) {
        String userId = actorContext.currentUser().requireAccountId();
        TipoMateria tipo = (request.tipo() != null && !request.tipo().isBlank())
                ? TipoMateria.fromString(request.tipo())
                : null;

        UpdateProposedMateriaCommand command = new UpdateProposedMateriaCommand(
                id,
                request.nombre(),
                request.codigo(),
                request.creditos(),
                tipo,
                userId
        );

        MateriaDetailDto updated = updateProposedMateriaHandler.execute(command);
        return Response.ok(updated).build();
    }
}

