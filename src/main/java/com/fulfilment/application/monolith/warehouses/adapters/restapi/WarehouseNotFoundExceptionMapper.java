package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WarehouseNotFoundExceptionMapper
    implements ExceptionMapper<WarehouseNotFoundException> {

  @Override
  public Response toResponse(WarehouseNotFoundException exception) {
    return Response.status(Response.Status.NOT_FOUND)
        .type(MediaType.APPLICATION_JSON)
        .entity(new WarehouseErrorResponse(404, exception.getMessage()))
        .build();
  }
}
