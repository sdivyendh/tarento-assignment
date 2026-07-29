package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseReplacementTransactionTest {

  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @InjectSpy WarehouseRepository warehouseRepository;

  @AfterEach
  void restoreRepositorySpy() {
    reset(warehouseRepository);
  }

  @Test
  void rollsBackPredecessorArchiveWhenSuccessorPersistenceFails() {
    Warehouse predecessor = warehouseRepository.findByBusinessUnitCode("MWH.012");
    assertNotNull(predecessor);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = predecessor.businessUnitCode;
    replacement.location = predecessor.location;
    replacement.capacity = predecessor.capacity;
    replacement.stock = predecessor.stock;

    doThrow(new IllegalStateException("simulated successor persistence failure"))
        .when(warehouseRepository)
        .create(any(Warehouse.class));

    assertThrows(
        IllegalStateException.class,
        () -> replaceWarehouseOperation.replace(replacement));
    verify(warehouseRepository).update(any(Warehouse.class));

    // Remove the failure stub before reading through the real repository again.
    reset(warehouseRepository);
    Warehouse stillActive =
        warehouseRepository.findByBusinessUnitCode(predecessor.businessUnitCode);

    assertNotNull(stillActive);
    assertEquals(predecessor.id, stillActive.id);
    assertNull(stillActive.archivedAt);
    assertEquals(
        1L,
        warehouseRepository.count("businessUnitCode", predecessor.businessUnitCode));
  }
}
