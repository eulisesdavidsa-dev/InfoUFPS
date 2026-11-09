package org.infoUFPS.Controllers.User;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.infoUFPS.Features.Calendar.Commands.*;
import org.infoUFPS.Features.Calendar.Queries.GetMyFavoriteIdsQuery;
import org.infoUFPS.Features.Calendar.Queries.GetMyFavoritesQuery;
import org.infoUFPS.Features.Calendar.Queries.GetMyProposalsQuery;
import org.infoUFPS.Security.ActorContext;
import org.infoUFPS.Security.CurrentUser;
import org.infoUFPS.Support.ApiParams;

/**
 * Endpoints del usuario autenticado.
 */
@Path("/api/v1/events")
@Produces(MediaType.APPLICATION_JSON)
public class UserEventController {

    @Inject
    ProposeEventHandler proposeHandler;

    @Inject
    UpdateEventHandler updateHandler;

    @Inject
    AddFavoriteHandler addFavoriteHandler;

    @Inject
    RemoveFavoriteHandler removeFavoriteHandler;

    @Inject
    GetMyProposalsQuery myProposalsQuery;

    @Inject
    GetMyFavoritesQuery myFavoritesQuery;

    @Inject
    GetMyFavoriteIdsQuery myFavoriteIdsQuery;

    /**
     * Propone un evento. Entra en revisión.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response propose(@BeanParam ActorContext actor, EventRequest request) {
        CurrentUser user = actor.currentUser();
        String accountId = user.requireAccountId();
        return Response.status(Response.Status.CREATED)
                .entity(proposeHandler.execute(new ProposeEventCommand(accountId, request)))
                .build();
    }

    /**
     * Lista las propuestas de la cuenta autenticada.
     */
    @GET
    @Path("/mine")
    public Response mine(@BeanParam ActorContext actor,
                         @QueryParam("page") String page,
                         @QueryParam("size") String size) {
        String accountId = actor.currentUser().requireAccountId();
        return Response.ok(myProposalsQuery.execute(
                accountId,
                ApiParams.optionalInt(page, "page", 0),
                ApiParams.optionalInt(size, "size", 20))).build();
    }

    /**
     * Edita una propuesta propia mientras está en revisión.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@BeanParam ActorContext actor,
                           @PathParam("id") String id,
                           EventRequest request) {
        CurrentUser user = actor.currentUser();
        String accountId = user.requireAccountId();
        return Response.ok(updateHandler.execute(
                new UpdateEventCommand(id, accountId, false, request))).build();
    }

    /**
     * Marca un evento publicado como favorito. Idempotente.
     */
    @PUT
    @Path("/{id}/favorite")
    public Response addFavorite(@BeanParam ActorContext actor, @PathParam("id") String id) {
        String accountId = actor.currentUser().requireAccountId();
        addFavoriteHandler.execute(new AddFavoriteCommand(accountId, id));
        return Response.noContent().build();
    }

    /**
     * Quita un evento de favoritos. Idempotente.
     */
    @DELETE
    @Path("/{id}/favorite")
    public Response removeFavorite(@BeanParam ActorContext actor, @PathParam("id") String id) {
        String accountId = actor.currentUser().requireAccountId();
        removeFavoriteHandler.execute(new RemoveFavoriteCommand(accountId, id));
        return Response.noContent().build();
    }

    /**
     * Lista los eventos publicados favoritos, ordenados por fecha del evento.
     */
    @GET
    @Path("/favorites")
    public Response favorites(@BeanParam ActorContext actor,
                              @QueryParam("page") String page,
                              @QueryParam("size") String size) {
        String accountId = actor.currentUser().requireAccountId();
        return Response.ok(myFavoritesQuery.execute(
                accountId,
                ApiParams.optionalInt(page, "page", 0),
                ApiParams.optionalInt(size, "size", 20))).build();
    }

    /**
     * Lista liviana de identificadores de eventos favoritos visibles.
     */
    @GET
    @Path("/favorites/ids")
    public Response favoriteIds(@BeanParam ActorContext actor) {
        String accountId = actor.currentUser().requireAccountId();
        return Response.ok(myFavoriteIdsQuery.execute(accountId)).build();
    }
}
