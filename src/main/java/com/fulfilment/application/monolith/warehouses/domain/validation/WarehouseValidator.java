package com.fulfilment.application.monolith.warehouses.domain.validation;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;

/** Centralises warehouse invariants so create and replace cannot drift apart over time. */
@ApplicationScoped
public class WarehouseValidator {

  private static final int MAX_IDENTIFIER_LENGTH = 40;

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public WarehouseValidator(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  /** Validates all rules that must hold before a new active warehouse is persisted. */
  public void validateForCreation(Warehouse warehouse) {
    validateRequiredFieldsAndStock(warehouse);

    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new WarehouseValidationException(
          "An active warehouse with business unit code '"
              + warehouse.businessUnitCode
              + "' already exists");
    }

    Location location = resolveLocation(warehouse.location);
    validateLocationLimits(warehouse, location, null);
  }

  /**
   * Validates a replacement, excluding the predecessor from location count and capacity totals.
   * This matters when a location is already exactly at one of its limits.
   */
  public void validateForReplacement(Warehouse replacement, Warehouse predecessor) {
    validateRequiredFieldsAndStock(replacement);

    if (!Objects.equals(predecessor.stock, replacement.stock)) {
      throw new WarehouseValidationException(
          "Replacement stock must match the current warehouse stock");
    }

    Location location = resolveLocation(replacement.location);
    validateLocationLimits(replacement, location, predecessor);
  }

  /** Validates just enough input to identify the active warehouse being replaced. */
  public void validateReplacementIdentifier(Warehouse replacement) {
    requireWarehouse(replacement);
    requireNonBlank(replacement.businessUnitCode, "Business unit code is required");
    requireMaximumLength(replacement.businessUnitCode, "Business unit code");
  }

  /** Validates the identifier carried by the existing archive operation contract. */
  public void validateArchiveIdentifier(Warehouse warehouse) {
    requireWarehouse(warehouse);
    if (warehouse.id == null) {
      throw new WarehouseValidationException("Warehouse id is required");
    }
  }

  private void validateRequiredFieldsAndStock(Warehouse warehouse) {
    requireWarehouse(warehouse);
    requireNonBlank(warehouse.businessUnitCode, "Business unit code is required");
    requireNonBlank(warehouse.location, "Location is required");
    requireMaximumLength(warehouse.businessUnitCode, "Business unit code");
    requireMaximumLength(warehouse.location, "Location");

    if (warehouse.capacity == null) {
      throw new WarehouseValidationException("Capacity is required");
    }
    if (warehouse.capacity <= 0) {
      throw new WarehouseValidationException("Capacity must be greater than zero");
    }
    if (warehouse.stock == null) {
      throw new WarehouseValidationException("Stock is required");
    }
    if (warehouse.stock < 0) {
      throw new WarehouseValidationException("Stock cannot be negative");
    }
    if (warehouse.capacity < warehouse.stock) {
      throw new WarehouseValidationException(
          "Warehouse capacity must be greater than or equal to its stock");
    }
  }

  private Location resolveLocation(String identifier) {
    Location location = locationResolver.resolveByIdentifier(identifier);
    if (location == null) {
      throw new WarehouseValidationException("Location '" + identifier + "' does not exist");
    }
    return location;
  }

  private void validateLocationLimits(
      Warehouse candidate, Location location, Warehouse warehouseToExclude) {
    List<Warehouse> activeAtLocation =
        safeWarehouses().stream()
            .filter(Objects::nonNull)
            // The store contract is active-only; this check is defensive for alternate adapters.
            .filter(Warehouse::isActive)
            .filter(warehouse -> Objects.equals(location.identification, warehouse.location))
            .filter(warehouse -> !isSameWarehouse(warehouse, warehouseToExclude))
            .toList();

    if (activeAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Location '"
              + location.identification
              + "' already has the maximum number of active warehouses");
    }

    long allocatedCapacity =
        activeAtLocation.stream()
            .map(warehouse -> warehouse.capacity)
            .filter(Objects::nonNull)
            .mapToLong(Integer::longValue)
            .sum();
    long capacityAfterChange = allocatedCapacity + candidate.capacity.longValue();

    if (capacityAfterChange > location.maxCapacity) {
      throw new WarehouseValidationException(
          "Combined warehouse capacity at location '"
              + location.identification
              + "' cannot exceed "
              + location.maxCapacity);
    }
  }

  private List<Warehouse> safeWarehouses() {
    List<Warehouse> warehouses = warehouseStore.getAll();
    return warehouses == null ? List.of() : warehouses;
  }

  private boolean isSameWarehouse(Warehouse candidate, Warehouse warehouseToExclude) {
    if (warehouseToExclude == null) {
      return false;
    }
    if (candidate.id != null && warehouseToExclude.id != null) {
      return Objects.equals(candidate.id, warehouseToExclude.id);
    }
    return Objects.equals(candidate.businessUnitCode, warehouseToExclude.businessUnitCode);
  }

  private void requireWarehouse(Warehouse warehouse) {
    if (warehouse == null) {
      throw new WarehouseValidationException("Warehouse is required");
    }
  }

  private void requireNonBlank(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new WarehouseValidationException(message);
    }
  }

  private void requireMaximumLength(String value, String fieldName) {
    if (value.length() > MAX_IDENTIFIER_LENGTH) {
      throw new WarehouseValidationException(
          fieldName + " cannot exceed " + MAX_IDENTIFIER_LENGTH + " characters");
    }
  }
}
