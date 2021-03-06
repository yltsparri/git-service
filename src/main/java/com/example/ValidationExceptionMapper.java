package com.example;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<javax.validation.ValidationException> {

    @Override
    public Response toResponse(ValidationException e) {
        final StringBuilder strBuilder = new StringBuilder();
        for (ConstraintViolation<?> cv : ((ConstraintViolationException) e).getConstraintViolations()) {
            strBuilder.append(cv.getMessage());
        }
        String message = strBuilder.toString();
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).type(MediaType.TEXT_PLAIN).entity(message)
                .build();
    }
}
