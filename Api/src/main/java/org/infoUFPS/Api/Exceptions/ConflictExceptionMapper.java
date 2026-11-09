package org.infoUFPS.Api.Exceptions;

import jakarta.ws.rs.core.MediaType;
import org.infoUFPS.Exceptions.ConflictException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce conflictos de negocio a HTTP 409.
 */
@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {

    @Override
    public Response toResponse(ConflictException exception) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError("CONFLICT", exception.getMessage()))
                .build();
    }
}
