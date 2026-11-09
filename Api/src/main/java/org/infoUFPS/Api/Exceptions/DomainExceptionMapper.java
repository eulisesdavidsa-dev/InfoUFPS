package org.infoUFPS.Api.Exceptions;

import jakarta.ws.rs.core.MediaType;
import org.infoUFPS.Exceptions.DomainException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce errores de validación de dominio a HTTP 400.
 */
@Provider
public class DomainExceptionMapper implements ExceptionMapper<DomainException> {

    @Override
    public Response toResponse(DomainException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError("BAD_REQUEST", exception.getMessage()))
                .build();
    }
}
