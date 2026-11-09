package org.infoUFPS.Controllers.Admin;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.DTOs.MateriaDetailDto;
import org.infoUFPS.DTOs.Requests.AdminSaveMateriaCommand;
import org.infoUFPS.Enums.TipoMateria;
import org.infoUFPS.Features.Materia.Commands.AdminCreateMateriaHandler;
import org.infoUFPS.Features.Materia.Commands.AdminUpdateMateriaHandler;
import org.infoUFPS.Security.AuthContext;

@Path("/api/v1/admin/materias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminMateriaController {

    @Inject
    AdminCreateMateriaHandler adminCreateMateriaHandler;

    @Inject
    AdminUpdateMateriaHandler adminUpdateMateriaHandler;

    @Inject
    AuthContext authContext;

    public record SaveMateriaRequest(
            String nombre,
            String codigo,
            Integer creditos,
            String tipo
    ) {}

    @POST
    public Response create(SaveMateriaRequest request) {
        authContext.requireAdmin();
        TipoMateria tipo = (request.tipo() != null && !request.tipo().isBlank())
                ? TipoMateria.fromString(request.tipo())
                : null;

        AdminSaveMateriaCommand command = new AdminSaveMateriaCommand(
                request.nombre(),
                request.codigo(),
                request.creditos(),
                tipo
        );

        MateriaDetailDto created = adminCreateMateriaHandler.execute(command);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, SaveMateriaRequest request) {
        authContext.requireAdmin();
        TipoMateria tipo = (request.tipo() != null && !request.tipo().isBlank())
                ? TipoMateria.fromString(request.tipo())
                : null;

        AdminSaveMateriaCommand command = new AdminSaveMateriaCommand(
                request.nombre(),
                request.codigo(),
                request.creditos(),
                tipo
        );

        MateriaDetailDto updated = adminUpdateMateriaHandler.execute(id, command);
        return Response.ok(updated).build();
    }
}

