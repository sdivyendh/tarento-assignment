package com.fulfilment.application.monolith.fulfilments;

import java.time.LocalDateTime;

public record FulfilmentAssignmentResponse(
    Long id,
    Long storeId,
    String storeName,
    Long productId,
    String productName,
    Long warehouseId,
    String warehouseBusinessUnitCode,
    LocalDateTime createdAt) {

  static FulfilmentAssignmentResponse from(FulfilmentAssignment assignment) {
    return new FulfilmentAssignmentResponse(
        assignment.id,
        assignment.store.id,
        assignment.store.name,
        assignment.product.id,
        assignment.product.name,
        assignment.warehouse.id,
        assignment.warehouse.businessUnitCode,
        assignment.createdAt);
  }
}
