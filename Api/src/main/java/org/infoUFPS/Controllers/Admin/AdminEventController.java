package org.infoUFPS.Controllers.Admin;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.Features.Calendar.Commands.*;
import org.infoUFPS.Features.Calendar.Queries.GetModerationQueueQuery;
import org.infoUFPS.Security.ActorContext;
import org.infoUFPS.Security.CurrentUser;
import org.infoUFPS.Support.ApiParams;

/**
 * Endpoints administrativos del calendario.
 */
@Path("/api/v1/admin/events")
@Produces(MediaType.APPLICATION_JSON)
public class AdminEventController {

    @Inject
    CreateEventHandler createHandler;

    @Inject
    UpdateEventHandler updateHandler;

    @Inject
    PublishEventHandler publishHandler;

    @Inject
    ArchiveEventHandler archiveHandler;

    @Inject
    GetModerationQueueQuery moderationQuery;

    /**
     * Crea un evento publicado. Solo administradores.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@BeanParam ActorContext actor, EventRequest request) {
        CurrentUser user = requireAdmin(actor);
        String accountId = user.requireAccountId();
        return Response.status(Response.Status.CREATED)
                .entity(createHandler.execute(new CreateEventCommand(request), accountId))
                .build();
    }

    /**
     * Corrige un evento. Solo administradores.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@BeanParam ActorContext actor,
                           @PathParam("id") String id,
                           EventRequest request) {
        CurrentUser user = requireAdmin(actor);
        String accountId = user.requireAccountId();
        return Response.ok(updateHandler.execute(
                new UpdateEventCommand(id, accountId, true, request))).build();
    }

    /**
     * Cola de moderación por estado.
     */
    @GET
    public Response queue(@BeanParam ActorContext actor,
                          @QueryParam("status") String status,
                          @QueryParam("page") String page,
                          @QueryParam("size") String size) {
        requireAdmin(actor);
        return Response.ok(moderationQuery.execute(
                status,
                ApiParams.optionalInt(page, "page", 0),
                ApiParams.optionalInt(size, "size", 20))).build();
    }

    /**
     * Publica un evento en revisión.
     */
    @POST
    @Path("/{id}/publish")
    public Response publish(@BeanParam ActorContext actor, @PathParam("id") String id) {
        requireAdmin(actor);
        return Response.ok(publishHandler.execute(new PublishEventCommand(id))).build();
    }

    /**
     * Archiva un evento.
     */
    @POST
    @Path("/{id}/archive")
    public Response archive(@BeanParam ActorContext actor, @PathParam("id") String id) {
        requireAdmin(actor);
        return Response.ok(archiveHandler.execute(new ArchiveEventCommand(id))).build();
    }

    private CurrentUser requireAdmin(ActorContext actor) {
        CurrentUser user = actor.currentUser();
        user.requireAdmin();
        return user;
    }
}
