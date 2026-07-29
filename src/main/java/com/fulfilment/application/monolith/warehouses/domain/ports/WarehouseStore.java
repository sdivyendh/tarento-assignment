package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.List;

public interface WarehouseStore {

  /** Returns only active warehouses. */
  List<Warehouse> getAll();

  void create(Warehouse warehouse);

  void update(Warehouse warehouse);

  /** Finds an active warehouse by its database identity, or returns {@code null}. */
  Warehouse findActiveById(Long id);

  /** Finds an active warehouse by its business identity, or returns {@code null}. */
  Warehouse findByBusinessUnitCode(String buCode);

  /**
   * Finds and locks an active warehouse for a state-changing operation.
   *
   * <p>The default preserves compatibility for non-database adapters; production persistence
   * overrides it with a database lock.
   */
  default Warehouse findActiveByIdForUpdate(Long id) {
    return findActiveById(id);
  }

  /** See {@link #findActiveByIdForUpdate(Long)}. */
  default Warehouse findByBusinessUnitCodeForUpdate(String buCode) {
    return findByBusinessUnitCode(buCode);
  }

  /**
   * Serializes changes that affect a location's warehouse count or allocated capacity.
   *
   * <p>In-memory adapters need no lock, hence the default no-op.
   */
  default void lockLocationForUpdate(String locationIdentifier) {}
}
