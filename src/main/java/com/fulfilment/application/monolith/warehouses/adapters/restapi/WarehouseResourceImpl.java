package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import java.util.List;
import org.jboss.resteasy.reactive.ResponseStatus;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject WarehouseStore warehouseStore;

  @Inject CreateWarehouseOperation createWarehouseOperation;

  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;

  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseStore.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  // Redeclared here so Quarkus applies the 201 status missing from the generated interface.
  @Override
  @POST
  @Produces("application/json")
  @Consumes("application/json")
  @ResponseStatus(201)
  public Warehouse createANewWarehouseUnit(Warehouse data) {
    if (data == null) {
      throw new WarehouseValidationException("A warehouse request body is required.");
    }
    if (data.getId() != null) {
      throw new WarehouseValidationException("The warehouse id must not be supplied on creation.");
    }

    var warehouse = toDomainWarehouse(data);
    createWarehouseOperation.create(warehouse);
    return toWarehouseResponse(warehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var warehouse = warehouseStore.findActiveById(toNumericId(id));
    if (warehouse == null) {
      throw new WarehouseNotFoundException("Active warehouse with id " + id + " was not found.");
    }
    return toWarehouseResponse(warehouse);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    var warehouse = warehouseStore.findActiveById(toNumericId(id));
    if (warehouse == null) {
      throw new WarehouseNotFoundException("Active warehouse with id " + id + " was not found.");
    }
    archiveWarehouseOperation.archive(warehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, Warehouse data) {
    if (data == null) {
      throw new WarehouseValidationException("A warehouse request body is required.");
    }
    if (data.getId() != null) {
      throw new WarehouseValidationException(
          "The replacement warehouse id must not be supplied.");
    }

    String bodyBusinessUnitCode = data.getBusinessUnitCode();
    if (bodyBusinessUnitCode != null && !bodyBusinessUnitCode.equals(businessUnitCode)) {
      throw new WarehouseValidationException(
          "The request body business unit code must match the path.");
    }

    var replacement = toDomainWarehouse(data);
    // The URL identifies the warehouse being replaced and is therefore authoritative.
    replacement.businessUnitCode = businessUnitCode;
    replaceWarehouseOperation.replace(replacement);
    return toWarehouseResponse(replacement);
  }

  private Long toNumericId(String id) {
    try {
      long value = Long.parseLong(id);
      if (value <= 0) {
        throw new NumberFormatException();
      }
      return value;
    } catch (NumberFormatException exception) {
      throw new WarehouseValidationException("Warehouse id must be a positive number.");
    }
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
      toDomainWarehouse(Warehouse request) {
    var warehouse =
        new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = request.getBusinessUnitCode();
    warehouse.location = request.getLocation();
    warehouse.capacity = request.getCapacity();
    warehouse.stock = request.getStock();
    return warehouse;
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    if (warehouse.id != null) {
      response.setId(warehouse.id.toString());
    }
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }
}
