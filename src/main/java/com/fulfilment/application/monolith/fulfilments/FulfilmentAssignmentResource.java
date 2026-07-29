package com.fulfilment.application.monolith.fulfilments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.net.URI;
import java.util.List;

@Path("fulfilment-assignments")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfilmentAssignmentResource {

  @Inject FulfilmentAssignmentService service;

  @POST
  public Response create(CreateFulfilmentAssignmentRequest request) {
    FulfilmentAssignmentResponse created = service.create(request);
    return Response.created(URI.create("/fulfilment-assignments/" + created.id()))
        .entity(created)
        .build();
  }

  @GET
  public List<FulfilmentAssignmentResponse> list(
      @QueryParam("storeId") Long storeId,
      @QueryParam("productId") Long productId,
      @QueryParam("warehouseId") Long warehouseId) {
    return service.list(storeId, productId, warehouseId);
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") Long id) {
    service.delete(id);
    return Response.noContent().build();
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<FulfilmentAssignmentException> {

    @Inject ObjectMapper objectMapper;

    @Override
    public Response toResponse(FulfilmentAssignmentException exception) {
      ObjectNode json = objectMapper.createObjectNode();
      json.put("exceptionType", exception.getClass().getName());
      json.put("code", exception.status);
      json.put("error", exception.getMessage());
      return Response.status(exception.status).entity(json).build();
    }
  }
}
