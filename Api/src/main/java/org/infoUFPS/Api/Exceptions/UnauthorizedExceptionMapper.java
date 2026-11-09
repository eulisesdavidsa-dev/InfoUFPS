package org.infoUFPS.Api.Exceptions;

import jakarta.ws.rs.core.MediaType;
import org.infoUFPS.Exceptions.UnauthorizedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce la falta de autenticación a HTTP 401.
 */
@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

    @Override
    public Response toResponse(UnauthorizedException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError("UNAUTHORIZED", exception.getMessage()))
                .build();
    }
}
