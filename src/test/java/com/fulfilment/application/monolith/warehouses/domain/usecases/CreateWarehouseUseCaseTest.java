package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.NOW;
import static com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.warehouse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.InMemoryWarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.WarehouseUseCaseTestSupport.StubLocationResolver;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CreateWarehouseUseCaseTest {

  private static final String LOCATION = "TEST-001";

  @Test
  void shouldCreateWarehouseAndOwnItsLifecycleFields() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    CreateWarehouseUseCase useCase = useCase(store, location(2, 100));
    Warehouse candidate = warehouse(99L, "MWH.100", LOCATION, 60, 25);
    candidate.createdAt = LocalDateTime.of(2000, 1, 1, 0, 0);
    candidate.archivedAt = LocalDateTime.of(2001, 1, 1, 0, 0);

    useCase.create(candidate);

    assertAll(
        () -> assertEquals(1, store.created.size()),
        () -> assertEquals(candidate, store.created.get(0)),
        () -> assertNull(candidate.id, "A new row must receive its identity from persistence"),
        () -> assertEquals(NOW, candidate.createdAt),
        () -> assertNull(candidate.archivedAt),
        () -> assertEquals(List.of(LOCATION), store.lockedLocations));
  }

  @Test
  void shouldRejectAnExistingActiveBusinessUnitCode() {
    Warehouse existing = warehouse(1L, "MWH.100", LOCATION, 20, 5);
    InMemoryWarehouseStore store = new InMemoryWarehouseStore(existing);
    CreateWarehouseUseCase useCase = useCase(store, location(3, 100));

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> useCase.create(warehouse(null, "MWH.100", LOCATION, 20, 5)));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("already exists")),
        () -> assertTrue(store.created.isEmpty()));
  }

  @Test
  void shouldAllowReuseOfBusinessUnitCodeWhenOnlyArchivedHistoryExists() {
    Warehouse archived = warehouse(1L, "MWH.100", LOCATION, 20, 5);
    archived.archivedAt = NOW.minusDays(1);
    InMemoryWarehouseStore store = new InMemoryWarehouseStore(archived);
    CreateWarehouseUseCase useCase = useCase(store, location(1, 20));

    useCase.create(warehouse(null, "MWH.100", LOCATION, 20, 5));

    assertEquals(1, store.created.size());
  }

  @Test
  void shouldRejectAnUnknownLocation() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    CreateWarehouseUseCase useCase = useCase(store, new StubLocationResolver());

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> useCase.create(warehouse(null, "MWH.100", "UNKNOWN", 20, 5)));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("does not exist")),
        () -> assertTrue(store.created.isEmpty()));
  }

  @Test
  void shouldRejectNullWarehouse() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    CreateWarehouseUseCase useCase = useCase(store, location(2, 100));

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.create(null));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("Warehouse is required")),
        () -> assertTrue(store.created.isEmpty()));
  }

  @Test
  void shouldRejectCreationWhenLocationWarehouseLimitHasBeenReached() {
    InMemoryWarehouseStore store =
        new InMemoryWarehouseStore(warehouse(1L, "MWH.001", LOCATION, 20, 5));
    CreateWarehouseUseCase useCase = useCase(store, location(1, 100));

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> useCase.create(warehouse(null, "MWH.002", LOCATION, 20, 5)));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("maximum number")),
        () -> assertTrue(store.created.isEmpty()));
  }

  @Test
  void shouldAllowCombinedCapacityExactlyAtLocationLimit() {
    InMemoryWarehouseStore store =
        new InMemoryWarehouseStore(warehouse(1L, "MWH.001", LOCATION, 40, 5));
    CreateWarehouseUseCase useCase = useCase(store, location(2, 100));

    useCase.create(warehouse(null, "MWH.002", LOCATION, 60, 5));

    assertEquals(1, store.created.size());
  }

  @Test
  void shouldRejectCombinedCapacityAboveLocationLimit() {
    InMemoryWarehouseStore store =
        new InMemoryWarehouseStore(warehouse(1L, "MWH.001", LOCATION, 41, 5));
    CreateWarehouseUseCase useCase = useCase(store, location(2, 100));

    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class,
            () -> useCase.create(warehouse(null, "MWH.002", LOCATION, 60, 5)));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("cannot exceed 100")),
        () -> assertTrue(store.created.isEmpty()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidWarehouses")
  void shouldRejectInvalidRequiredCapacityAndStockFields(
      String scenario, Consumer<Warehouse> invalidChange, String expectedMessage) {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    CreateWarehouseUseCase useCase = useCase(store, location(2, 100));
    Warehouse candidate = warehouse(null, "MWH.100", LOCATION, 20, 5);
    invalidChange.accept(candidate);

    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> useCase.create(candidate));

    assertAll(
        () -> assertTrue(exception.getMessage().contains(expectedMessage)),
        () -> assertTrue(store.created.isEmpty()));
  }

  private static Stream<Arguments> invalidWarehouses() {
    return Stream.of(
        Arguments.of(
            "missing business unit code",
            (Consumer<Warehouse>) warehouse -> warehouse.businessUnitCode = null,
            "Business unit code is required"),
        Arguments.of(
            "blank business unit code",
            (Consumer<Warehouse>) warehouse -> warehouse.businessUnitCode = "   ",
            "Business unit code is required"),
        Arguments.of(
            "business unit code above database limit",
            (Consumer<Warehouse>) warehouse -> warehouse.businessUnitCode = "M".repeat(41),
            "cannot exceed 40"),
        Arguments.of(
            "missing location",
            (Consumer<Warehouse>) warehouse -> warehouse.location = null,
            "Location is required"),
        Arguments.of(
            "location above database limit",
            (Consumer<Warehouse>) warehouse -> warehouse.location = "L".repeat(41),
            "cannot exceed 40"),
        Arguments.of(
            "missing capacity",
            (Consumer<Warehouse>) warehouse -> warehouse.capacity = null,
            "Capacity is required"),
        Arguments.of(
            "zero capacity",
            (Consumer<Warehouse>) warehouse -> warehouse.capacity = 0,
            "greater than zero"),
        Arguments.of(
            "negative capacity",
            (Consumer<Warehouse>) warehouse -> warehouse.capacity = -1,
            "greater than zero"),
        Arguments.of(
            "missing stock",
            (Consumer<Warehouse>) warehouse -> warehouse.stock = null,
            "Stock is required"),
        Arguments.of(
            "negative stock",
            (Consumer<Warehouse>) warehouse -> warehouse.stock = -1,
            "cannot be negative"),
        Arguments.of(
            "stock above capacity",
            (Consumer<Warehouse>) warehouse -> warehouse.stock = 21,
            "greater than or equal"));
  }

  private CreateWarehouseUseCase useCase(
      InMemoryWarehouseStore store, StubLocationResolver resolver) {
    return new CreateWarehouseUseCase(store, new WarehouseValidator(store, resolver), () -> NOW);
  }

  private StubLocationResolver location(int maxWarehouses, int maxCapacity) {
    return new StubLocationResolver().withLocation(LOCATION, maxWarehouses, maxCapacity);
  }
}
