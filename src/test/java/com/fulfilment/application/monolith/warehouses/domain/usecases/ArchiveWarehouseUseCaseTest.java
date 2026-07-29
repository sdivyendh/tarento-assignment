package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.NOW;
import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.NO_OP_FULFILMENT_ASSIGNMENTS;
import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.warehouse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.InMemoryWarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.StubLocationResolver;
import org.junit.jupiter.api.Test;

class ArchiveWarehouseUseCaseTest {

  @Test
  void shouldArchiveThePersistedActiveWarehouseAtCurrentUtcTime() {
    Warehouse persisted = warehouse(7L, "MWH.007", "TEST-001", 50, 10);
    InMemoryWarehouseStore store = new InMemoryWarehouseStore(persisted);
    ArchiveWarehouseUseCase useCase = useCase(store);
    Warehouse command = new Warehouse();
    command.id = 7L;

    useCase.archive(command);

    assertAll(
        () -> assertEquals(NOW, persisted.archivedAt),
        () -> assertEquals(1, store.updated.size()),
        () -> assertSame(persisted, store.updated.get(0)));
  }

  @Test
  void shouldRejectArchiveWithoutAnId() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    ArchiveWarehouseUseCase useCase = useCase(store);

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.archive(new Warehouse()));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("id is required")),
        () -> assertTrue(store.updated.isEmpty()));
  }

  @Test
  void shouldRejectNullArchiveCommand() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    ArchiveWarehouseUseCase useCase = useCase(store);

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.archive(null));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("Warehouse is required")),
        () -> assertTrue(store.updated.isEmpty()));
  }

  @Test
  void shouldReportMissingOrAlreadyArchivedWarehouseAsNotFound() {
    Warehouse archived = warehouse(7L, "MWH.007", "TEST-001", 50, 10);
    archived.archivedAt = NOW.minusDays(1);
    InMemoryWarehouseStore store = new InMemoryWarehouseStore(archived);
    ArchiveWarehouseUseCase useCase = useCase(store);
    Warehouse command = new Warehouse();
    command.id = 7L;

    WarehouseNotFoundException exception =
        assertThrows(WarehouseNotFoundException.class, () -> useCase.archive(command));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("id '7'")),
        () -> assertTrue(store.updated.isEmpty()));
  }

  private ArchiveWarehouseUseCase useCase(InMemoryWarehouseStore store) {
    WarehouseValidator validator =
        new WarehouseValidator(store, new StubLocationResolver());
    return new ArchiveWarehouseUseCase(
        store, validator, () -> NOW, NO_OP_FULFILMENT_ASSIGNMENTS);
  }
}
