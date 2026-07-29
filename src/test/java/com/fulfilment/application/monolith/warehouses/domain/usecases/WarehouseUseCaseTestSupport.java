package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseFulfilmentAssignments;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class WarehouseUseCaseTestSupport {

  static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 29, 10, 15, 30);

  private WarehouseUseCaseTestSupport() {}

  static final WarehouseFulfilmentAssignments NO_OP_FULFILMENT_ASSIGNMENTS =
      new WarehouseFulfilmentAssignments() {
        @Override
        public void deleteForWarehouse(Long warehouseId) {}

        @Override
        public void transferWarehouse(Long currentWarehouseId, Long replacementWarehouseId) {}
      };

  static Warehouse warehouse(
      Long id, String businessUnitCode, String location, Integer capacity, Integer stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.id = id;
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);
    return warehouse;
  }

  static final class StubLocationResolver implements LocationResolver {
    private final Map<String, Location> locations = new HashMap<>();

    StubLocationResolver withLocation(
        String identification, int maxNumberOfWarehouses, int maxCapacity) {
      locations.put(
          identification,
          new Location(identification, maxNumberOfWarehouses, maxCapacity));
      return this;
    }

    @Override
    public Location resolveByIdentifier(String identifier) {
      return locations.get(identifier);
    }
  }

  /**
   * A deliberately small in-memory port implementation. These are pure unit tests: they exercise
   * domain policy without starting Quarkus, Hibernate, or a database.
   */
  static final class InMemoryWarehouseStore implements WarehouseStore {
    final List<Warehouse> records = new ArrayList<>();
    final List<Warehouse> created = new ArrayList<>();
    final List<Warehouse> updated = new ArrayList<>();
    final List<String> operationOrder = new ArrayList<>();
    final List<String> lockedLocations = new ArrayList<>();

    InMemoryWarehouseStore(Warehouse... warehouses) {
      records.addAll(List.of(warehouses));
    }

    @Override
    public List<Warehouse> getAll() {
      return records.stream().filter(Warehouse::isActive).toList();
    }

    @Override
    public void create(Warehouse warehouse) {
      operationOrder.add("create");
      created.add(warehouse);
      records.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      operationOrder.add("update");
      updated.add(warehouse);
    }

    @Override
    public Warehouse findActiveById(Long id) {
      return records.stream()
          .filter(Warehouse::isActive)
          .filter(warehouse -> Objects.equals(warehouse.id, id))
          .findFirst()
          .orElse(null);
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return records.stream()
          .filter(Warehouse::isActive)
          .filter(warehouse -> Objects.equals(warehouse.businessUnitCode, buCode))
          .findFirst()
          .orElse(null);
    }

    @Override
    public void lockLocationForUpdate(String locationIdentifier) {
      lockedLocations.add(locationIdentifier);
    }
  }
}
