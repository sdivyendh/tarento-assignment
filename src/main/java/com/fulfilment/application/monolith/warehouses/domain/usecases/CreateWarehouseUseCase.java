package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.time.TimeProvider;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator warehouseValidator;
  private final TimeProvider timeProvider;

  @Inject
  public CreateWarehouseUseCase(
      WarehouseStore warehouseStore,
      WarehouseValidator warehouseValidator,
      TimeProvider timeProvider) {
    this.warehouseStore = warehouseStore;
    this.warehouseValidator = warehouseValidator;
    this.timeProvider = timeProvider;
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    if (warehouse != null) {
      // The database lock makes location count/capacity checks safe across concurrent requests.
      warehouseStore.lockLocationForUpdate(warehouse.location);
    }
    warehouseValidator.validateForCreation(warehouse);

    // Identity and lifecycle fields are controlled by the application, not by the API caller.
    warehouse.id = null;
    warehouse.createdAt = timeProvider.now();
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);
  }
}
