package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WarehouseValidationExceptionMapper
    implements ExceptionMapper<WarehouseValidationException> {

  @Override
  public Response toResponse(WarehouseValidationException exception) {
    return Response.status(Response.Status.BAD_REQUEST)
        .type(MediaType.APPLICATION_JSON)
        .entity(new WarehouseErrorResponse(400, exception.getMessage()))
        .build();
  }
}
