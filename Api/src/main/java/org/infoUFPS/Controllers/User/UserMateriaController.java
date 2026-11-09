package org.infoUFPS.Controllers.User;

import jakarta.inject.Inject;
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
import org.infoUFPS.Security.AuthContext;

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
    AuthContext authContext;

    public record ProposeMateriaRequest(String nombre, String codigo, Integer creditos, String tipo) {}
    public record UpdateMateriaProposalRequest(String nombre, String codigo, Integer creditos, String tipo) {}

    @POST
    public Response propose(ProposeMateriaRequest request) {
        String userId = authContext.requireAuthenticatedUserId();
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
        String userId = authContext.requireAuthenticatedUserId();
        List<MateriaDetailDto> proposals = getMateriaPropuestasQuery.execute(userId);
        return Response.ok(proposals).build();
    }

    @PUT
    @Path("/propuestas/{id}")
    public Response updateProposal(@PathParam("id") String id, UpdateMateriaProposalRequest request) {
        String userId = authContext.requireAuthenticatedUserId();
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

