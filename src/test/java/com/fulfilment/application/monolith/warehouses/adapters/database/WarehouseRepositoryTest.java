package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseRepositoryTest {

  @Inject WarehouseRepository repository;

  @Test
  @TestTransaction
  void createsAndReadsAnActiveWarehouse() {
    Warehouse warehouse = warehouse("MWH.REPOSITORY.001");

    repository.create(warehouse);

    assertNotNull(warehouse.id);
    Warehouse persisted = repository.findActiveById(warehouse.id);
    assertNotNull(persisted);
    assertEquals(warehouse.businessUnitCode, persisted.businessUnitCode);
    assertEquals(warehouse.location, persisted.location);
    assertEquals(warehouse.capacity, persisted.capacity);
    assertEquals(warehouse.stock, persisted.stock);
    assertEquals(warehouse.createdAt, persisted.createdAt);
  }

  @Test
  @TestTransaction
  void archiveUpdatePreservesHistoryButRemovesWarehouseFromActiveViews() {
    Warehouse warehouse = warehouse("MWH.REPOSITORY.002");
    repository.create(warehouse);

    warehouse.archivedAt = LocalDateTime.of(2026, 1, 2, 3, 4);
    repository.update(warehouse);

    assertNull(repository.findActiveById(warehouse.id));
    assertNull(repository.findByBusinessUnitCode(warehouse.businessUnitCode));
    assertTrue(repository.getAll().stream().noneMatch(item -> item.id.equals(warehouse.id)));

    DbWarehouse historicalRecord = repository.findById(warehouse.id);
    assertNotNull(historicalRecord);
    assertEquals(warehouse.archivedAt, historicalRecord.archivedAt);
  }

  @Test
  @TestTransaction
  void databasePreventsTwoActiveRowsWithTheSameBusinessUnitCode() {
    Warehouse duplicate = warehouse("MWH.012");

    assertThrows(WarehouseValidationException.class, () -> repository.create(duplicate));
  }

  private Warehouse warehouse(String businessUnitCode) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = "AMSTERDAM-002";
    warehouse.capacity = 20;
    warehouse.stock = 5;
    warehouse.createdAt = LocalDateTime.of(2026, 1, 1, 1, 2);
    return warehouse;
  }
}
