package org.infoUFPS.Controllers.Admin;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.DTOs.ProfesorDetailDto;
import org.infoUFPS.DTOs.Requests.AdminSaveProfesorCommand;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Features.Profesor.Commands.AdminCreateProfesorHandler;
import org.infoUFPS.Features.Profesor.Commands.AdminUpdateProfesorHandler;
import org.infoUFPS.Features.Profesor.Commands.AssociateMateriasToProfesorHandler;
import org.infoUFPS.Security.ActorContext;

import java.util.Set;

@Path("/api/v1/admin/profesores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminProfesorController {

    @Inject
    AdminCreateProfesorHandler adminCreateProfesorHandler;

    @Inject
    AdminUpdateProfesorHandler adminUpdateProfesorHandler;

    @Inject
    AssociateMateriasToProfesorHandler associateMateriasToProfesorHandler;

    @Inject
    ActorContext actorContext;

    public record SaveProfesorRequest(
            @NotBlank @Size(max = 150) String nombre,
            String facultad,
            @Email @Size(max = 150) String email,
            Set<String> materiasIds
    ) {}

    public record AssociateMateriasRequest(
            Set<String> materiasIds
    ) {}

    @POST
    public Response create(@Valid SaveProfesorRequest request) {
        actorContext.currentUser().requireAdmin();
        Facultad facultad = (request.facultad() != null && !request.facultad().isBlank())
                ? Facultad.fromString(request.facultad())
                : Facultad.NO_ESPECIFICADA;

        AdminSaveProfesorCommand command = new AdminSaveProfesorCommand(
                request.nombre(),
                facultad,
                request.email(),
                request.materiasIds()
        );

        ProfesorDetailDto created = adminCreateProfesorHandler.execute(command);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, @Valid SaveProfesorRequest request) {
        actorContext.currentUser().requireAdmin();
        Facultad facultad = (request.facultad() != null && !request.facultad().isBlank())
                ? Facultad.fromString(request.facultad())
                : null;

        AdminSaveProfesorCommand command = new AdminSaveProfesorCommand(
                request.nombre(),
                facultad,
                request.email(),
                request.materiasIds()
        );

        ProfesorDetailDto updated = adminUpdateProfesorHandler.execute(id, command);
        return Response.ok(updated).build();
    }

    @POST
    @Path("/{id}/materias")
    public Response associateMaterias(@PathParam("id") String id, AssociateMateriasRequest request) {
        actorContext.currentUser().requireAdmin();
        ProfesorDetailDto updated = associateMateriasToProfesorHandler.execute(id, request.materiasIds());
        return Response.ok(updated).build();
    }
}

