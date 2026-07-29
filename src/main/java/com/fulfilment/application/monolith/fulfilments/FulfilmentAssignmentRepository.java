package com.fulfilment.application.monolith.fulfilments;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class FulfilmentAssignmentRepository
    implements PanacheRepository<FulfilmentAssignment> {

  boolean exists(Long storeId, Long productId, Long warehouseId) {
    return count(
            "store.id = ?1 and product.id = ?2 and warehouse.id = ?3",
            storeId,
            productId,
            warehouseId)
        > 0;
  }

  long countWarehousesForProductAtStore(Long storeId, Long productId) {
    return getEntityManager()
        .createQuery(
            """
            select count(distinct assignment.warehouse.id)
            from FulfilmentAssignment assignment
            where assignment.store.id = :storeId and assignment.product.id = :productId
            """,
            Long.class)
        .setParameter("storeId", storeId)
        .setParameter("productId", productId)
        .getSingleResult();
  }

  long countWarehousesForStore(Long storeId) {
    return getEntityManager()
        .createQuery(
            """
            select count(distinct assignment.warehouse.id)
            from FulfilmentAssignment assignment
            where assignment.store.id = :storeId
            """,
            Long.class)
        .setParameter("storeId", storeId)
        .getSingleResult();
  }

  boolean storeUsesWarehouse(Long storeId, Long warehouseId) {
    return count("store.id = ?1 and warehouse.id = ?2", storeId, warehouseId) > 0;
  }

  long countProductsForWarehouse(Long warehouseId) {
    return getEntityManager()
        .createQuery(
            """
            select count(distinct assignment.product.id)
            from FulfilmentAssignment assignment
            where assignment.warehouse.id = :warehouseId
            """,
            Long.class)
        .setParameter("warehouseId", warehouseId)
        .getSingleResult();
  }

  boolean warehouseStoresProduct(Long warehouseId, Long productId) {
    return count("warehouse.id = ?1 and product.id = ?2", warehouseId, productId) > 0;
  }

  List<FulfilmentAssignment> findFiltered(
      Long storeId, Long productId, Long warehouseId) {
    StringBuilder query = new StringBuilder("1 = 1");
    Map<String, Object> parameters = new HashMap<>();

    if (storeId != null) {
      query.append(" and store.id = :storeId");
      parameters.put("storeId", storeId);
    }
    if (productId != null) {
      query.append(" and product.id = :productId");
      parameters.put("productId", productId);
    }
    if (warehouseId != null) {
      query.append(" and warehouse.id = :warehouseId");
      parameters.put("warehouseId", warehouseId);
    }

    query.append(" order by store.name, product.name, warehouse.businessUnitCode");
    return find(query.toString(), parameters).list();
  }

  void deleteForProduct(Long productId) {
    delete("product.id", productId);
  }

  void deleteForStore(Long storeId) {
    delete("store.id", storeId);
  }

  void deleteForWarehouse(Long warehouseId) {
    delete("warehouse.id", warehouseId);
  }

  void transferWarehouse(Long currentWarehouseId, Long replacementWarehouseId) {
    update(
        "warehouse.id = ?1 where warehouse.id = ?2",
        replacementWarehouseId,
        currentWarehouseId);
  }
}
