package com.fulfilment.application.monolith.fulfilments;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseFulfilmentAssignments;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class FulfilmentAssignmentService implements WarehouseFulfilmentAssignments {

  static final int MAX_WAREHOUSES_PER_PRODUCT_AND_STORE = 2;
  static final int MAX_WAREHOUSES_PER_STORE = 3;
  static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  @Inject EntityManager entityManager;

  @Inject FulfilmentAssignmentRepository repository;

  @Transactional
  public FulfilmentAssignmentResponse create(CreateFulfilmentAssignmentRequest request) {
    validate(request);

    /*
     * Every writer locks in Store -> Warehouse order. This serializes competing assignments for
     * the two constrained aggregates and avoids deadlocks caused by inconsistent lock ordering.
     */
    Store store = entityManager.find(Store.class, request.storeId, LockModeType.PESSIMISTIC_WRITE);
    if (store == null) {
      throw FulfilmentAssignmentException.notFound(
          "Store with id " + request.storeId + " does not exist.");
    }

    Product product = entityManager.find(Product.class, request.productId);
    if (product == null) {
      throw FulfilmentAssignmentException.notFound(
          "Product with id " + request.productId + " does not exist.");
    }

    DbWarehouse warehouse =
        entityManager.find(
            DbWarehouse.class, request.warehouseId, LockModeType.PESSIMISTIC_WRITE);
    if (warehouse == null || warehouse.archivedAt != null) {
      throw FulfilmentAssignmentException.notFound(
          "Active warehouse with id " + request.warehouseId + " does not exist.");
    }

    if (repository.exists(request.storeId, request.productId, request.warehouseId)) {
      throw FulfilmentAssignmentException.conflict(
          "This store, product, and warehouse assignment already exists.");
    }

    if (repository.countWarehousesForProductAtStore(request.storeId, request.productId)
        >= MAX_WAREHOUSES_PER_PRODUCT_AND_STORE) {
      throw FulfilmentAssignmentException.conflict(
          "A product can be fulfilled by at most 2 different warehouses per store.");
    }

    if (!repository.storeUsesWarehouse(request.storeId, request.warehouseId)
        && repository.countWarehousesForStore(request.storeId) >= MAX_WAREHOUSES_PER_STORE) {
      throw FulfilmentAssignmentException.conflict(
          "A store can be fulfilled by at most 3 different warehouses.");
    }

    if (!repository.warehouseStoresProduct(request.warehouseId, request.productId)
        && repository.countProductsForWarehouse(request.warehouseId)
            >= MAX_PRODUCTS_PER_WAREHOUSE) {
      throw FulfilmentAssignmentException.conflict(
          "A warehouse can store at most 5 different product types.");
    }

    var assignment = new FulfilmentAssignment();
    assignment.store = store;
    assignment.product = product;
    assignment.warehouse = warehouse;
    assignment.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    repository.persistAndFlush(assignment);
    return FulfilmentAssignmentResponse.from(assignment);
  }

  @Transactional
  public List<FulfilmentAssignmentResponse> list(
      Long storeId, Long productId, Long warehouseId) {
    validateOptionalId("storeId", storeId);
    validateOptionalId("productId", productId);
    validateOptionalId("warehouseId", warehouseId);
    return repository.findFiltered(storeId, productId, warehouseId).stream()
        .map(FulfilmentAssignmentResponse::from)
        .toList();
  }

  @Transactional
  public void delete(Long assignmentId) {
    if (assignmentId == null || assignmentId <= 0) {
      throw FulfilmentAssignmentException.badRequest(
          "Fulfilment assignment id must be a positive number.");
    }
    if (!repository.deleteById(assignmentId)) {
      throw FulfilmentAssignmentException.notFound(
          "Fulfilment assignment with id " + assignmentId + " does not exist.");
    }
  }

  public void deleteForProduct(Long productId) {
    repository.deleteForProduct(productId);
  }

  public void deleteForStore(Long storeId) {
    repository.deleteForStore(storeId);
  }

  @Override
  public void deleteForWarehouse(Long warehouseId) {
    repository.deleteForWarehouse(warehouseId);
  }

  @Override
  public void transferWarehouse(Long currentWarehouseId, Long replacementWarehouseId) {
    repository.transferWarehouse(currentWarehouseId, replacementWarehouseId);
  }

  private void validate(CreateFulfilmentAssignmentRequest request) {
    if (request == null) {
      throw FulfilmentAssignmentException.badRequest("A request body is required.");
    }
    validateRequiredId("storeId", request.storeId);
    validateRequiredId("productId", request.productId);
    validateRequiredId("warehouseId", request.warehouseId);
  }

  private void validateRequiredId(String field, Long id) {
    if (id == null || id <= 0) {
      throw FulfilmentAssignmentException.badRequest(
          field + " must be a positive number.");
    }
  }

  private void validateOptionalId(String field, Long id) {
    if (id != null && id <= 0) {
      throw FulfilmentAssignmentException.badRequest(
          field + " must be a positive number.");
    }
  }
}
