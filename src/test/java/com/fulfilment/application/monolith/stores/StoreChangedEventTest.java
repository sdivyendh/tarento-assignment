package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StoreChangedEventTest {

  @Test
  void shouldKeepAnImmutableSnapshotOfTheManagedStore() {
    Store store = new Store("original-name");
    store.id = 42L;
    store.quantityProductsInStock = 7;

    StoreChangedEvent event = StoreChangedEvent.updated(store);
    store.name = "changed-after-event";
    store.quantityProductsInStock = 99;

    assertAll(
        () -> assertEquals(StoreChangedEvent.Operation.UPDATED, event.operation()),
        () -> assertEquals(42L, event.id()),
        () -> assertEquals("original-name", event.name()),
        () -> assertEquals(7, event.quantityProductsInStock()));
  }
}
