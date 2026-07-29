package com.fulfilment.application.monolith.warehouses.domain.time;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Production time source. Warehouse timestamps are consistently recorded in UTC. */
@ApplicationScoped
public class UtcTimeProvider implements TimeProvider {

  private final Clock clock;

  public UtcTimeProvider() {
    this(Clock.systemUTC());
  }

  UtcTimeProvider(Clock clock) {
    this.clock = clock;
  }

  @Override
  public LocalDateTime now() {
    return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
  }
}
