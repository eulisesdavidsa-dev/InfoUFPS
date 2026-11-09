package org.infoUFPS.Controllers.Admin;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.DTOs.MateriaDetailDto;
import org.infoUFPS.DTOs.ModeracionWarningDto;
import org.infoUFPS.DTOs.ProfesorDetailDto;
import org.infoUFPS.DTOs.Requests.ModerateEntityCommand;
import org.infoUFPS.DTOs.ResenaDto;
import org.infoUFPS.Enums.EstadoModeracion;
import org.infoUFPS.Enums.Facultad;
import org.infoUFPS.Enums.TipoDestino;
import org.infoUFPS.Enums.TipoMateria;
import org.infoUFPS.Features.Moderacion.Commands.ModerateMateriaHandler;
import org.infoUFPS.Features.Moderacion.Commands.ModerateProfesorHandler;
import org.infoUFPS.Features.Moderacion.Commands.ModerateResenaHandler;
import org.infoUFPS.Features.Moderacion.Queries.GetModeracionMateriasQuery;
import org.infoUFPS.Features.Moderacion.Queries.GetModeracionProfesoresQuery;
import org.infoUFPS.Features.Moderacion.Queries.GetModeracionResenasQuery;
import org.infoUFPS.Pagination.PageRequest;
import org.infoUFPS.Pagination.PageResult;
import org.infoUFPS.Security.AuthContext;
import org.infoUFPS.Services.SimilarityService;

import java.util.List;

@Path("/api/v1/admin/moderacion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminModeracionController {

    @Inject
    GetModeracionProfesoresQuery getModeracionProfesoresQuery;

    @Inject
    GetModeracionMateriasQuery getModeracionMateriasQuery;

    @Inject
    GetModeracionResenasQuery getModeracionResenasQuery;

    @Inject
    ModerateProfesorHandler moderateProfesorHandler;

    @Inject
    ModerateMateriaHandler moderateMateriaHandler;

    @Inject
    ModerateResenaHandler moderateResenaHandler;

    @Inject
    SimilarityService similarityService;

    @Inject
    AuthContext authContext;

    public record CorrectAndPublishProfesorRequest(
            String nuevoNombre,
            String nuevaFacultad,
            String nuevoEmail
    ) {}

    public record CorrectAndPublishMateriaRequest(
            String nuevoNombre,
            String nuevoCodigo,
            Integer nuevosCreditos,
            String nuevoTipo
    ) {}

    // ── Colas de Profesores ──────────────────────────────────────────────

    @GET
    @Path("/profesores")
    public Response getProfesoresQueue(
            @QueryParam("estado") String estadoStr,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        authContext.requireAdmin();
        EstadoModeracion estado = (estadoStr != null && !estadoStr.isBlank())
                ? EstadoModeracion.fromString(estadoStr)
                : EstadoModeracion.EN_REVISION;

        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<ProfesorDetailDto> result = getModeracionProfesoresQuery.execute(estado, pageRequest);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/profesores/{id}/publicar")
    public Response publishProfesor(@PathParam("id") String id, CorrectAndPublishProfesorRequest request) {
        authContext.requireAdmin();
        ModerateEntityCommand command = null;
        if (request != null) {
            Facultad fac = (request.nuevaFacultad() != null && !request.nuevaFacultad().isBlank())
                    ? Facultad.fromString(request.nuevaFacultad())
                    : null;
            command = new ModerateEntityCommand(request.nuevoNombre(), fac, request.nuevoEmail(), null, null, null);
        }
        ProfesorDetailDto updated = moderateProfesorHandler.publish(id, command);
        return Response.ok(updated).build();
    }

    @PUT
    @Path("/profesores/{id}/archivar")
    public Response archiveProfesor(@PathParam("id") String id) {
        authContext.requireAdmin();
        ProfesorDetailDto updated = moderateProfesorHandler.archive(id);
        return Response.ok(updated).build();
    }

    @GET
    @Path("/profesores/similares")
    public Response checkProfesorSimilarity(@QueryParam("nombre") String nombre, @QueryParam("facultad") String facultadStr) {
        authContext.requireAdmin();
        Facultad fac = (facultadStr != null && !facultadStr.isBlank()) ? Facultad.fromString(facultadStr) : null;
        List<ModeracionWarningDto> warnings = similarityService.checkProfesorSimilarity(nombre, fac);
        return Response.ok(warnings).build();
    }

    // ── Colas de Materias ────────────────────────────────────────────────

    @GET
    @Path("/materias")
    public Response getMateriasQueue(
            @QueryParam("estado") String estadoStr,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        authContext.requireAdmin();
        EstadoModeracion estado = (estadoStr != null && !estadoStr.isBlank())
                ? EstadoModeracion.fromString(estadoStr)
                : EstadoModeracion.EN_REVISION;

        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<MateriaDetailDto> result = getModeracionMateriasQuery.execute(estado, pageRequest);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/materias/{id}/publicar")
    public Response publishMateria(@PathParam("id") String id, CorrectAndPublishMateriaRequest request) {
        authContext.requireAdmin();
        ModerateEntityCommand command = null;
        if (request != null) {
            TipoMateria tipo = (request.nuevoTipo() != null && !request.nuevoTipo().isBlank())
                    ? TipoMateria.fromString(request.nuevoTipo())
                    : null;
            command = new ModerateEntityCommand(
                    request.nuevoNombre(),
                    null,
                    null,
                    request.nuevoCodigo(),
                    request.nuevosCreditos(),
                    tipo
            );
        }
        MateriaDetailDto updated = moderateMateriaHandler.publish(id, command);
        return Response.ok(updated).build();
    }

    @PUT
    @Path("/materias/{id}/archivar")
    public Response archiveMateria(@PathParam("id") String id) {
        authContext.requireAdmin();
        MateriaDetailDto updated = moderateMateriaHandler.archive(id);
        return Response.ok(updated).build();
    }

    @GET
    @Path("/materias/similares")
    public Response checkMateriaSimilarity(@QueryParam("nombre") String nombre) {
        authContext.requireAdmin();
        List<ModeracionWarningDto> warnings = similarityService.checkMateriaSimilarity(nombre);
        return Response.ok(warnings).build();
    }

    // ── Colas de Reseñas ─────────────────────────────────────────────────

    @GET
    @Path("/resenas")
    public Response getResenasQueue(
            @QueryParam("destino") String destinoStr,
            @QueryParam("estado") String estadoStr,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    ) {
        authContext.requireAdmin();
        TipoDestino destino = (destinoStr != null && !destinoStr.isBlank())
                ? TipoDestino.fromString(destinoStr)
                : null;
        EstadoModeracion estado = (estadoStr != null && !estadoStr.isBlank())
                ? EstadoModeracion.fromString(estadoStr)
                : EstadoModeracion.EN_REVISION;

        PageRequest pageRequest = PageRequest.of(page, size);
        PageResult<ResenaDto> result = getModeracionResenasQuery.execute(destino, estado, pageRequest);
        return Response.ok(result).build();
    }

    @PUT
    @Path("/resenas/{id}/publicar")
    public Response publishResena(@PathParam("id") String id) {
        authContext.requireAdmin();
        ResenaDto updated = moderateResenaHandler.publish(id);
        return Response.ok(updated).build();
    }

    @PUT
    @Path("/resenas/{id}/archivar")
    public Response archiveResena(@PathParam("id") String id) {
        authContext.requireAdmin();
        ResenaDto updated = moderateResenaHandler.archive(id);
        return Response.ok(updated).build();
    }
}

