package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseFulfilmentAssignments;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.time.TimeProvider;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Objects;
import java.util.stream.Stream;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator warehouseValidator;
  private final TimeProvider timeProvider;
  private final WarehouseFulfilmentAssignments fulfilmentAssignments;

  @Inject
  public ReplaceWarehouseUseCase(
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
  public void replace(Warehouse newWarehouse) {
    warehouseValidator.validateReplacementIdentifier(newWarehouse);

    Warehouse currentWarehouse =
        warehouseStore.findByBusinessUnitCodeForUpdate(newWarehouse.businessUnitCode);
    if (currentWarehouse == null) {
      throw WarehouseNotFoundException.forBusinessUnitCode(newWarehouse.businessUnitCode);
    }

    lockAffectedLocations(currentWarehouse, newWarehouse);
    warehouseValidator.validateForReplacement(newWarehouse, currentWarehouse);

    // One timestamp makes the lifecycle transition unambiguous in history/audit views.
    var replacementTime = timeProvider.now();
    currentWarehouse.archivedAt = replacementTime;

    // A replacement is a new physical warehouse row sharing only the business identity.
    newWarehouse.id = null;
    newWarehouse.createdAt = replacementTime;
    newWarehouse.archivedAt = null;

    // @Transactional makes these two writes atomic: creation failure rolls archive back.
    warehouseStore.update(currentWarehouse);
    warehouseStore.create(newWarehouse);
    fulfilmentAssignments.transferWarehouse(currentWarehouse.id, newWarehouse.id);
  }

  private void lockAffectedLocations(Warehouse currentWarehouse, Warehouse newWarehouse) {
    /*
     * Deterministic ordering prevents two cross-location replacements from deadlocking while each
     * waits for the location lock held by the other.
     */
    Stream.of(currentWarehouse.location, newWarehouse.location)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .forEach(warehouseStore::lockLocationForUpdate);
  }
}
