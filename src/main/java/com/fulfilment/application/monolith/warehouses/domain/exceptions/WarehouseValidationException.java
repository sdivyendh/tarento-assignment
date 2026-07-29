package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/**
 * Raised when a warehouse command violates an input or business rule.
 *
 * <p>Keeping this exception independent of HTTP lets the REST adapter decide how domain failures
 * are represented to API clients.
 */
public class WarehouseValidationException extends RuntimeException {

  public WarehouseValidationException(String message) {
    super(message);
  }
}
