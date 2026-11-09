package org.infoUFPS.Api.Exceptions;

import jakarta.ws.rs.core.MediaType;
import org.infoUFPS.Exceptions.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce recursos inexistentes a HTTP 404.
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError("NOT_FOUND", exception.getMessage()))
                .build();
    }
}
