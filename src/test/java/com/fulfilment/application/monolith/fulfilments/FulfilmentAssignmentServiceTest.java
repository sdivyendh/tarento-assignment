package com.fulfilment.application.monolith.fulfilments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfilmentAssignmentServiceTest {

  @Inject FulfilmentAssignmentService service;

  @Inject EntityManager entityManager;

  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;

  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @Test
  @TestTransaction
  void limitsAProductToTwoWarehousesAtTheSameStore() {
    Store store = store("BONUS PRODUCT LIMIT STORE");
    Product product = product("BONUS PRODUCT LIMIT");
    DbWarehouse first = warehouse("MWH.BONUS.P1");
    DbWarehouse second = warehouse("MWH.BONUS.P2");
    DbWarehouse third = warehouse("MWH.BONUS.P3");
    entityManager.flush();

    service.create(request(store, product, first));
    service.create(request(store, product, second));

    FulfilmentAssignmentException exception =
        assertThrows(
            FulfilmentAssignmentException.class,
            () -> service.create(request(store, product, third)));

    assertEquals(409, exception.status);
    assertTrue(exception.getMessage().contains("at most 2"));
  }

  @Test
  @TestTransaction
  void limitsAStoreToThreeDistinctWarehouses() {
    Store store = store("BONUS STORE LIMIT");
    Product firstProduct = product("BONUS STORE PRODUCT 1");
    Product secondProduct = product("BONUS STORE PRODUCT 2");
    Product thirdProduct = product("BONUS STORE PRODUCT 3");
    Product fourthProduct = product("BONUS STORE PRODUCT 4");
    DbWarehouse first = warehouse("MWH.BONUS.S1");
    DbWarehouse second = warehouse("MWH.BONUS.S2");
    DbWarehouse third = warehouse("MWH.BONUS.S3");
    DbWarehouse fourth = warehouse("MWH.BONUS.S4");
    entityManager.flush();

    service.create(request(store, firstProduct, first));
    service.create(request(store, secondProduct, second));
    service.create(request(store, thirdProduct, third));

    FulfilmentAssignmentException exception =
        assertThrows(
            FulfilmentAssignmentException.class,
            () -> service.create(request(store, fourthProduct, fourth)));

    assertEquals(409, exception.status);
    assertTrue(exception.getMessage().contains("at most 3"));
  }

  @Test
  @TestTransaction
  void limitsAWarehouseToFiveDistinctProductTypes() {
    Store store = store("BONUS WAREHOUSE LIMIT STORE");
    DbWarehouse warehouse = warehouse("MWH.BONUS.W1");
    Product[] products = new Product[6];
    for (int index = 0; index < products.length; index++) {
      products[index] = product("BONUS WAREHOUSE PRODUCT " + index);
    }
    entityManager.flush();

    for (int index = 0; index < 5; index++) {
      service.create(request(store, products[index], warehouse));
    }

    FulfilmentAssignmentException exception =
        assertThrows(
            FulfilmentAssignmentException.class,
            () -> service.create(request(store, products[5], warehouse)));

    assertEquals(409, exception.status);
    assertTrue(exception.getMessage().contains("at most 5"));
  }

  @Test
  @TestTransaction
  void reusingAnExistingStoreWarehouseAndWarehouseProductDoesNotConsumeDistinctLimits() {
    Store store = store("BONUS DISTINCT STORE");
    Product sharedProduct = product("BONUS DISTINCT SHARED");
    DbWarehouse sharedWarehouse = warehouse("MWH.BONUS.DISTINCT.SHARED");
    entityManager.flush();

    service.create(request(store, sharedProduct, sharedWarehouse));
    for (int index = 0; index < 4; index++) {
      service.create(
          request(store, productAndFlush("BONUS DISTINCT EXTRA " + index), sharedWarehouse));
    }

    Store anotherStore = storeAndFlush("BONUS DISTINCT ANOTHER STORE");
    FulfilmentAssignmentResponse created =
        service.create(request(anotherStore, sharedProduct, sharedWarehouse));

    assertEquals(sharedWarehouse.id, created.warehouseId());
    assertEquals(6, service.list(null, null, sharedWarehouse.id).size());
  }

  @Test
  @TestTransaction
  void removesAssignmentsWhenAWarehouseIsArchived() {
    Store store = store("BONUS ARCHIVE STORE");
    Product product = product("BONUS ARCHIVE PRODUCT");
    DbWarehouse warehouse = warehouse("MWH.BONUS.ARCHIVE");
    entityManager.flush();
    service.create(request(store, product, warehouse));

    var archive =
        new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    archive.id = warehouse.id;
    archiveWarehouseOperation.archive(archive);

    assertTrue(service.list(null, null, warehouse.id).isEmpty());
  }

  @Test
  @TestTransaction
  void transfersAssignmentsToAReplacementWarehouse() {
    Store store = store("BONUS REPLACE STORE");
    Product product = product("BONUS REPLACE PRODUCT");
    DbWarehouse predecessor = warehouse("MWH.BONUS.REPLACE");
    entityManager.flush();
    service.create(request(store, product, predecessor));

    var replacement =
        new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    replacement.businessUnitCode = predecessor.businessUnitCode;
    replacement.location = predecessor.location;
    replacement.capacity = predecessor.capacity;
    replacement.stock = predecessor.stock;
    replaceWarehouseOperation.replace(replacement);

    assertTrue(service.list(null, null, predecessor.id).isEmpty());
    assertEquals(1, service.list(null, null, replacement.id).size());
  }

  private Store store(String name) {
    Store store = new Store(name);
    entityManager.persist(store);
    return store;
  }

  private Store storeAndFlush(String name) {
    Store store = store(name);
    entityManager.flush();
    return store;
  }

  private Product product(String name) {
    Product product = new Product(name);
    entityManager.persist(product);
    return product;
  }

  private Product productAndFlush(String name) {
    Product product = product(name);
    entityManager.flush();
    return product;
  }

  private DbWarehouse warehouse(String businessUnitCode) {
    var warehouse = new DbWarehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.activeBusinessUnitCode = businessUnitCode;
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 20;
    warehouse.stock = 5;
    warehouse.createdAt = LocalDateTime.of(2026, 7, 29, 10, 0);
    entityManager.persist(warehouse);
    return warehouse;
  }

  private CreateFulfilmentAssignmentRequest request(
      Store store, Product product, DbWarehouse warehouse) {
    var request = new CreateFulfilmentAssignmentRequest();
    request.storeId = store.id;
    request.productId = product.id;
    request.warehouseId = warehouse.id;
    return request;
  }
}
