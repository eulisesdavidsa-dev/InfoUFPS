package org.infoUFPS.ExceptionMappers;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.infoUFPS.Exceptions.DailyLimitExceededException;

@Provider
public class DailyLimitExceededExceptionMapper implements ExceptionMapper<DailyLimitExceededException> {

    @Override
    public Response toResponse(DailyLimitExceededException exception) {
        ErrorResponse body = ErrorResponse.of("DAILY_LIMIT_EXCEEDED", exception.getMessage(), 400);
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}

