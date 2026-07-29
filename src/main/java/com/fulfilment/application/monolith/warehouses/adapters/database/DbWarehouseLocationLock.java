package com.fulfilment.application.monolith.warehouses.adapters.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Provides one database row per valid location on which capacity-changing operations can take a
 * pessimistic lock. This makes count and aggregate-capacity validation safe across application
 * instances.
 */
@Entity
@Table(name = "warehouse_location_lock")
public class DbWarehouseLocationLock {

  @Id
  @Column(length = 40)
  public String identifier;

  public DbWarehouseLocationLock() {}
}
