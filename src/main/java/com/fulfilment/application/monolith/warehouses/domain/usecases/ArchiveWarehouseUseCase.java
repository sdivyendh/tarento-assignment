package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseFulfilmentAssignments;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.time.TimeProvider;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator warehouseValidator;
  private final TimeProvider timeProvider;
  private final WarehouseFulfilmentAssignments fulfilmentAssignments;

  @Inject
  public ArchiveWarehouseUseCase(
      WarehouseStore warehouseStore,
      WarehouseValidator warehouseValidator,
      TimeProvider timeProvider,
      WarehouseFulfilmentAssignments fulfilmentAssignments) {
    this.warehouseStore = warehouseStore;
    this.warehouseValidator = warehouseValidator;
    this.timeProvider = timeProvider;
    this.fulfilmentAssignments = fulfilmentAssignments;
  }

  @Override
  @Transactional
  public void archive(Warehouse warehouse) {
    warehouseValidator.validateArchiveIdentifier(warehouse);

    Warehouse activeWarehouse = warehouseStore.findActiveByIdForUpdate(warehouse.id);
    if (activeWarehouse == null) {
      throw WarehouseNotFoundException.forId(warehouse.id);
    }

    activeWarehouse.archivedAt = timeProvider.now();
    warehouseStore.update(activeWarehouse);
    fulfilmentAssignments.deleteForWarehouse(activeWarehouse.id);
  }
}
