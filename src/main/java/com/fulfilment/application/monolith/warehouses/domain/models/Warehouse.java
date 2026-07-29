package com.fulfilment.application.monolith.warehouses.domain.models;

import java.time.LocalDateTime;

public class Warehouse {

  // Database identity. Replacements receive a new id while retaining the business unit code.
  public Long id;

  // Business identity of the warehouse.
  public String businessUnitCode;

  public String location;

  public Integer capacity;

  public Integer stock;

  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;

  /**
   * Archived warehouses are retained for audit/history purposes but are not part of normal reads
   * or location limit calculations.
   */
  public boolean isActive() {
    return archivedAt == null;
  }
}
