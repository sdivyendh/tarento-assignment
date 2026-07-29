package com.fulfilment.application.monolith.warehouses.domain.time;

import java.time.LocalDateTime;

/**
 * Supplies domain timestamps.
 *
 * <p>The abstraction keeps use cases deterministic in tests and prevents application code from
 * accidentally using the server's local time zone.
 */
@FunctionalInterface
public interface TimeProvider {

  LocalDateTime now();
}
