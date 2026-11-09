package org.infoUFPS.Api.Exceptions;

import jakarta.ws.rs.core.MediaType;
import org.infoUFPS.Exceptions.ForbiddenException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce la falta de permisos a HTTP 403.
 */
@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Override
    public Response toResponse(ForbiddenException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError("FORBIDDEN", exception.getMessage()))
                .build();
    }
}
