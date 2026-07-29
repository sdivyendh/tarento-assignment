package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Inject EntityManager entityManager;

  @Override
  public List<Warehouse> getAll() {
    return list("archivedAt is null", Sort.by("businessUnitCode"))
        .stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    var entity = DbWarehouse.fromWarehouse(warehouse);
    try {
      persist(entity);
      flush();
    } catch (RuntimeException exception) {
      if (hasCause(exception, org.hibernate.exception.ConstraintViolationException.class)) {
        /*
         * Validation normally catches duplicates. The database constraint closes the race between
         * concurrent requests, and this translation keeps that losing request a domain error.
         */
        throw new WarehouseValidationException(
            "An active warehouse with business unit code '"
                + warehouse.businessUnitCode
                + "' already exists");
      }
      throw exception;
    }

    // The domain object is also the use-case result, so propagate the generated identifier.
    warehouse.id = entity.id;
  }

  @Override
  public void update(Warehouse warehouse) {
    if (warehouse.id == null) {
      throw new IllegalArgumentException("A warehouse id is required for an update.");
    }

    var entity = findById(warehouse.id);
    if (entity == null) {
      throw new WarehouseNotFoundException("Warehouse with id " + warehouse.id + " was not found.");
    }

    entity.copyFrom(warehouse);
    flush();
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse entity = find("activeBusinessUnitCode", buCode).firstResult();
    return entity == null ? null : entity.toWarehouse();
  }

  @Override
  public Warehouse findActiveById(Long id) {
    if (id == null) {
      return null;
    }

    DbWarehouse entity = find("id = ?1 and archivedAt is null", id).firstResult();
    return entity == null ? null : entity.toWarehouse();
  }

  @Override
  public Warehouse findActiveByIdForUpdate(Long id) {
    if (id == null) {
      return null;
    }

    DbWarehouse entity =
        find("id = ?1 and archivedAt is null", id)
            .withLock(LockModeType.PESSIMISTIC_WRITE)
            .firstResult();
    return entity == null ? null : entity.toWarehouse();
  }

  @Override
  public Warehouse findByBusinessUnitCodeForUpdate(String buCode) {
    DbWarehouse entity =
        find("activeBusinessUnitCode", buCode)
            .withLock(LockModeType.PESSIMISTIC_WRITE)
            .firstResult();
    return entity == null ? null : entity.toWarehouse();
  }

  @Override
  public void lockLocationForUpdate(String locationIdentifier) {
    if (locationIdentifier != null) {
      entityManager.find(
          DbWarehouseLocationLock.class,
          locationIdentifier,
          LockModeType.PESSIMISTIC_WRITE);
    }
  }

  private boolean hasCause(Throwable throwable, Class<? extends Throwable> expectedType) {
    Throwable current = throwable;
    while (current != null) {
      if (expectedType.isInstance(current)) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }
}
