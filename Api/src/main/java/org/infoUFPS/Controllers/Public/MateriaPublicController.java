package org.infoUFPS.Controllers.Public;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.DTOs.PublicMateriaDetailDto;
import org.infoUFPS.DTOs.PublicMateriaListItemDto;
import org.infoUFPS.DTOs.PublicResenaDto;
import org.infoUFPS.Enums.TipoMateria;
import org.infoUFPS.Features.Materia.Queries.GetMateriaDetailQuery;
import org.infoUFPS.Features.Materia.Queries.GetMateriasPublicQuery;
import org.infoUFPS.Features.Resena.Queries.GetResenasByDestinoQuery;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Security.ActorContext;
import org.infoUFPS.Security.CurrentUser;
import org.infoUFPS.ValueObjects.AccountId;

@Path("/api/v1/public/materias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MateriaPublicController {

    @Inject
    GetMateriasPublicQuery getMateriasPublicQuery;

    @Inject
    GetMateriaDetailQuery getMateriaDetailQuery;

    @Inject
    GetResenasByDestinoQuery getResenasByDestinoQuery;

    @Inject
    ActorContext actorContext;

    @GET
    public Response list(
            @QueryParam("q") String query,
            @QueryParam("creditos") Integer creditos,
            @QueryParam("tipo") String tipoStr,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        TipoMateria tipo = null;
        if (tipoStr != null && !tipoStr.isBlank()) {
            tipo = TipoMateria.fromString(tipoStr);
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<PublicMateriaListItemDto> result = getMateriasPublicQuery.execute(query, creditos, tipo, pageRequest);
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    public Response getDetail(@PathParam("id") String id) {
        CurrentUser user = actorContext.currentUser();
        AccountId requesterId = user.accountId() != null ? AccountId.of(user.accountId()) : null;
        boolean isAdmin = user.admin();
        PublicMateriaDetailDto detail = getMateriaDetailQuery.execute(id, requesterId, isAdmin);
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
        PageResult<PublicResenaDto> result = getResenasByDestinoQuery.getByMateria(id, pageRequest);
        return Response.ok(result).build();
    }
}

