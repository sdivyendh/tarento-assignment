package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(FlywaySchemaTest.FlywayProfile.class)
class FlywaySchemaTest {

  @Inject WarehouseRepository warehouseRepository;

  @Inject EntityManager entityManager;

  @Test
  void migrationCreatesASchemaThatMatchesTheJpaModel() {
    // Quarkus startup performs Hibernate "validate"; reaching this assertion proves compatibility.
    assertEquals(3L, warehouseRepository.count());
    assertEquals(3L, countRows("Store"));
    assertEquals(3L, countRows("Product"));
    assertEquals(8L, countRows("warehouse_location_lock"));
  }

  private long countRows(String tableName) {
    Number rowCount =
        (Number)
            entityManager
                .createNativeQuery("select count(*) from " + tableName)
                .getSingleResult();
    return rowCount.longValue();
  }

  public static class FlywayProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          "quarkus.datasource.jdbc.url",
          "jdbc:h2:mem:flyway-schema;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
          "quarkus.hibernate-orm.database.generation",
          "validate",
          "quarkus.hibernate-orm.sql-load-script",
          "no-file",
          "quarkus.flyway.migrate-at-start",
          "true");
    }
  }
}
