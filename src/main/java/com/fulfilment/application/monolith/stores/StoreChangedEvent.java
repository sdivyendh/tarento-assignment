package com.fulfilment.application.monolith.stores;

import java.util.Objects;

/**
 * Immutable data captured while a store transaction is still active.
 *
 * <p>A managed Hibernate entity must not be handed to post-commit code: it is mutable and is
 * detached by the time that code runs. This event therefore contains only the values the legacy
 * system needs.
 */
public record StoreChangedEvent(
    Operation operation, Long id, String name, int quantityProductsInStock) {

  public StoreChangedEvent {
    Objects.requireNonNull(operation, "operation must not be null");
    Objects.requireNonNull(id, "persisted store id must not be null");
  }

  public static StoreChangedEvent created(Store store) {
    return snapshot(Operation.CREATED, store);
  }

  public static StoreChangedEvent updated(Store store) {
    return snapshot(Operation.UPDATED, store);
  }

  private static StoreChangedEvent snapshot(Operation operation, Store store) {
    Objects.requireNonNull(store, "store must not be null");
    return new StoreChangedEvent(
        operation, store.id, store.name, store.quantityProductsInStock);
  }

  public enum Operation {
    CREATED,
    UPDATED
  }
}
