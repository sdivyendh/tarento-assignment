package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.NO_OP_FULFILMENT_ASSIGNMENTS;
import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.NOW;
import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.warehouse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.InMemoryWarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.StubLocationResolver;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReplaceWarehouseUseCaseTest {

  private static final String LOCATION = "TEST-001";

  @Test
  void shouldArchivePredecessorAndCreateDistinctSuccessorAtomically() {
    Warehouse predecessor = warehouse(7L, "MWH.007", LOCATION, 50, 10);
    InMemoryWarehouseStore store = new InMemoryWarehouseStore(predecessor);
    ReplaceWarehouseUseCase useCase = useCase(store, location(1, 60));
    Warehouse replacement = warehouse(999L, "MWH.007", LOCATION, 60, 10);
    replacement.createdAt = LocalDateTime.of(2000, 1, 1, 0, 0);
    replacement.archivedAt = NOW.minusDays(1);

    useCase.replace(replacement);

    assertAll(
        () -> assertEquals(NOW, predecessor.archivedAt),
        () -> assertEquals(NOW, replacement.createdAt),
        () -> assertNull(replacement.archivedAt),
        () -> assertNull(replacement.id, "Replacement must be inserted as a new row"),
        () -> assertEquals(List.of("update", "create"), store.operationOrder),
        () -> assertEquals(List.of(LOCATION), store.lockedLocations),
        () -> assertSame(predecessor, store.updated.get(0)),
        () -> assertSame(replacement, store.created.get(0)));
  }

  @Test
  void shouldRejectReplacementWhenActivePredecessorDoesNotExist() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    ReplaceWarehouseUseCase useCase = useCase(store, location(1, 100));

    WarehouseNotFoundException exception =
        assertThrows(
            WarehouseNotFoundException.class,
            () -> useCase.replace(warehouse(null, "MWH.404", LOCATION, 50, 10)));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("MWH.404")),
        () -> assertTrue(store.updated.isEmpty()),
        () -> assertTrue(store.created.isEmpty()));
  }

  @Test
  void shouldRejectReplacementWhoseStockDoesNotExactlyMatchPredecessor() {
    Warehouse predecessor = warehouse(7L, "MWH.007", LOCATION, 50, 10);
    InMemoryWarehouseStore store = new InMemoryWarehouseStore(predecessor);
    ReplaceWarehouseUseCase useCase = useCase(store, location(1, 100));

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> useCase.replace(warehouse(null, "MWH.007", LOCATION, 50, 9)));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("stock must match")),
        () -> assertNull(predecessor.archivedAt),
        () -> assertTrue(store.operationOrder.isEmpty()));
  }

  @Test
  void shouldRejectReplacementWhoseCapacityCannotAccommodateStock() {
    Warehouse predecessor = warehouse(7L, "MWH.007", LOCATION, 50, 20);
    InMemoryWarehouseStore store = new InMemoryWarehouseStore(predecessor);
    ReplaceWarehouseUseCase useCase = useCase(store, location(1, 100));

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> useCase.replace(warehouse(null, "MWH.007", LOCATION, 19, 20)));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("greater than or equal")),
        () -> assertNull(predecessor.archivedAt),
        () -> assertTrue(store.operationOrder.isEmpty()));
  }

  @Test
  void shouldExcludePredecessorFromSameLocationCountAndCapacity() {
    Warehouse predecessor = warehouse(7L, "MWH.007", LOCATION, 100, 10);
    InMemoryWarehouseStore store = new InMemoryWarehouseStore(predecessor);
    ReplaceWarehouseUseCase useCase = useCase(store, location(1, 100));

    useCase.replace(warehouse(null, "MWH.007", LOCATION, 100, 10));

    assertEquals(List.of("update", "create"), store.operationOrder);
  }

  @Test
  void shouldIncludeOtherActiveWarehousesInReplacementLocationLimits() {
    Warehouse predecessor = warehouse(7L, "MWH.007", LOCATION, 40, 10);
    Warehouse neighbour = warehouse(8L, "MWH.008", LOCATION, 40, 5);
    InMemoryWarehouseStore store = new InMemoryWarehouseStore(predecessor, neighbour);
    ReplaceWarehouseUseCase useCase = useCase(store, location(2, 100));

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> useCase.replace(warehouse(null, "MWH.007", LOCATION, 61, 10)));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("cannot exceed 100")),
        () -> assertNull(predecessor.archivedAt),
        () -> assertTrue(store.operationOrder.isEmpty()));
  }

  @Test
  void shouldValidateLimitsAtNewLocationWhenReplacementMoves() {
    Warehouse predecessor = warehouse(7L, "MWH.007", LOCATION, 50, 10);
    Warehouse atDestination = warehouse(8L, "MWH.008", "TEST-002", 30, 5);
    InMemoryWarehouseStore store = new InMemoryWarehouseStore(predecessor, atDestination);
    StubLocationResolver locations =
        location(2, 100).withLocation("TEST-002", 1, 100);
    ReplaceWarehouseUseCase useCase = useCase(store, locations);

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> useCase.replace(warehouse(null, "MWH.007", "TEST-002", 50, 10)));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("maximum number")),
        () -> assertNull(predecessor.archivedAt),
        () -> assertEquals(List.of("TEST-001", "TEST-002"), store.lockedLocations),
        () -> assertTrue(store.operationOrder.isEmpty()));
  }

  @Test
  void shouldRejectMissingReplacementIdentifierBeforeRepositoryLookup() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    ReplaceWarehouseUseCase useCase = useCase(store, location(1, 100));
    Warehouse replacement = warehouse(null, " ", LOCATION, 50, 10);

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("Business unit code is required")),
        () -> assertTrue(store.operationOrder.isEmpty()));
  }

  @Test
  void shouldRejectUnknownReplacementLocationBeforeArchivingPredecessor() {
    Warehouse predecessor = warehouse(7L, "MWH.007", LOCATION, 50, 10);
    InMemoryWarehouseStore store = new InMemoryWarehouseStore(predecessor);
    ReplaceWarehouseUseCase useCase = useCase(store, location(1, 100));

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> useCase.replace(warehouse(null, "MWH.007", "UNKNOWN", 50, 10)));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("does not exist")),
        () -> assertNull(predecessor.archivedAt),
        () -> assertTrue(store.operationOrder.isEmpty()));
  }

  private ReplaceWarehouseUseCase useCase(
      InMemoryWarehouseStore store, StubLocationResolver resolver) {
    return new ReplaceWarehouseUseCase(
        store,
        new WarehouseValidator(store, resolver),
        () -> NOW,
        NO_OP_FULFILMENT_ASSIGNMENTS);
  }

  private StubLocationResolver location(int maxWarehouses, int maxCapacity) {
    return new StubLocationResolver().withLocation(LOCATION, maxWarehouses, maxCapacity);
  }
}
