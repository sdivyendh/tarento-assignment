package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse")
@Cacheable
public class DbWarehouse {

  @Id @GeneratedValue public Long id;

  @Column(nullable = false, length = 40)
  public String businessUnitCode;

  /*
   * A nullable unique projection enforces "one active row per business unit" in both PostgreSQL
   * and H2. Archived rows set this to null, so any number of historical rows may retain the code.
   */
  @Column(length = 40, unique = true)
  public String activeBusinessUnitCode;

  @Column(nullable = false, length = 40)
  public String location;

  @Column(nullable = false)
  public Integer capacity;

  @Column(nullable = false)
  public Integer stock;

  @Column(nullable = false)
  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;

  public DbWarehouse() {}

  public static DbWarehouse fromWarehouse(Warehouse warehouse) {
    var entity = new DbWarehouse();
    entity.copyFrom(warehouse);
    return entity;
  }

  public void copyFrom(Warehouse warehouse) {
    this.businessUnitCode = warehouse.businessUnitCode;
    this.activeBusinessUnitCode =
        warehouse.archivedAt == null ? warehouse.businessUnitCode : null;
    this.location = warehouse.location;
    this.capacity = warehouse.capacity;
    this.stock = warehouse.stock;
    this.createdAt = warehouse.createdAt;
    this.archivedAt = warehouse.archivedAt;
  }

  public Warehouse toWarehouse() {
    var warehouse = new Warehouse();
    warehouse.id = this.id;
    warehouse.businessUnitCode = this.businessUnitCode;
    warehouse.location = this.location;
    warehouse.capacity = this.capacity;
    warehouse.stock = this.stock;
    warehouse.createdAt = this.createdAt;
    warehouse.archivedAt = this.archivedAt;
    return warehouse;
  }
}
