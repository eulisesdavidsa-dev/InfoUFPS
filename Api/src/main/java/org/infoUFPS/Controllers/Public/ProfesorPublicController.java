package org.infoUFPS.Controllers.Public;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.DTOs.PublicProfesorDetailDto;
import org.infoUFPS.DTOs.PublicProfesorListItemDto;
import org.infoUFPS.DTOs.PublicResenaDto;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Features.Profesor.Queries.GetProfesorDetailQuery;
import org.infoUFPS.Features.Profesor.Queries.GetProfesoresPublicQuery;
import org.infoUFPS.Features.Resena.Queries.GetResenasByDestinoQuery;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Security.ActorContext;
import org.infoUFPS.Security.CurrentUser;
import org.infoUFPS.ValueObjects.AccountId;

@Path("/api/v1/public/profesores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfesorPublicController {

    @Inject
    GetProfesoresPublicQuery getProfesoresPublicQuery;

    @Inject
    GetProfesorDetailQuery getProfesorDetailQuery;

    @Inject
    GetResenasByDestinoQuery getResenasByDestinoQuery;

    @Inject
    ActorContext actorContext;

    @GET
    public Response list(
            @QueryParam("q") String query,
            @QueryParam("facultad") String facultadStr,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        Facultad facultad = null;
        if (facultadStr != null && !facultadStr.isBlank()) {
            facultad = Facultad.fromString(facultadStr);
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<PublicProfesorListItemDto> result = getProfesoresPublicQuery.execute(query, facultad, pageRequest);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    public Response getDetail(@PathParam("id") String id) {
        CurrentUser user = actorContext.currentUser();
        AccountId requesterId = user.accountId() != null ? AccountId.of(user.accountId()) : null;
        boolean isAdmin = user.admin();
        PublicProfesorDetailDto detail = getProfesorDetailQuery.execute(id, requesterId, isAdmin);
        return Response.ok(detail).build();
    }

    @GET
    @Path("/{id}/resenas")
    public Response getResenas(
            @PathParam("id") String id,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<PublicResenaDto> result = getResenasByDestinoQuery.getByProfesor(id, pageRequest);
        return Response.ok(result).build();
    }
}

