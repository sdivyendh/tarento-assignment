package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/** Raised when an operation requires an active warehouse that does not exist. */
public class WarehouseNotFoundException extends RuntimeException {

  public WarehouseNotFoundException(String message) {
    super(message);
  }

  public static WarehouseNotFoundException forId(Long id) {
    return new WarehouseNotFoundException("Active warehouse with id '" + id + "' was not found");
  }

  public static WarehouseNotFoundException forBusinessUnitCode(String businessUnitCode) {
    return new WarehouseNotFoundException(
        "Active warehouse with business unit code '"
            + businessUnitCode
            + "' was not found");
  }
}
