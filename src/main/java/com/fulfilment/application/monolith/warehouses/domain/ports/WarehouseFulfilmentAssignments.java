package com.fulfilment.application.monolith.warehouses.domain.ports;

/**
 * Keeps fulfilment assignments consistent with warehouse lifecycle operations.
 *
 * <p>The warehouse domain depends only on this port; the fulfilment feature supplies the database
 * implementation.
 */
public interface WarehouseFulfilmentAssignments {

  void deleteForWarehouse(Long warehouseId);

  void transferWarehouse(Long currentWarehouseId, Long replacementWarehouseId);
}
