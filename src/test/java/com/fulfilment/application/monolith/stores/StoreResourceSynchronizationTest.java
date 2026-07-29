package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
class StoreResourceSynchronizationTest {

  @Inject StoreResource storeResource;

  @InjectMock LegacyStoreManagerGateway legacyStoreManagerGateway;

  @BeforeEach
  void clearLegacyCalls() {
    clearInvocations(legacyStoreManagerGateway);
  }

  @Test
  void shouldCreateOnLegacySystemOnlyAfterTheStoreTransactionCommits() {
    Store request = store(uniqueName("create"));
    request.quantityProductsInStock = 12;

    try (Response response = storeResource.create(request)) {
      assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    ArgumentCaptor<Store> committedStore = ArgumentCaptor.forClass(Store.class);
    verify(legacyStoreManagerGateway).createStoreOnLegacySystem(committedStore.capture());

    Store synchronizedStore = committedStore.getValue();
    assertAll(
        () -> assertNotNull(request.id),
        () -> assertEquals(request.id, synchronizedStore.id),
        () -> assertEquals(request.name, synchronizedStore.name),
        () ->
            assertEquals(
                request.quantityProductsInStock,
                synchronizedStore.quantityProductsInStock));
  }

  @Test
  void shouldKeepLegacySystemUntouchedUntilAnOuterTransactionCommits() {
    Store request = store(uniqueName("delayed-create"));

    QuarkusTransaction.requiringNew()
        .run(
            () -> {
              try (Response ignored = storeResource.create(request)) {
                verifyNoInteractions(legacyStoreManagerGateway);
              }
            });

    verify(legacyStoreManagerGateway)
        .createStoreOnLegacySystem(org.mockito.ArgumentMatchers.any(Store.class));
  }

  @Test
  void shouldNotNotifyLegacySystemWhenTheDatabaseTransactionRollsBack() {
    String rolledBackName = uniqueName("rollback");
    Store rolledBackStore = store(rolledBackName);

    assertThrows(
        IllegalStateException.class,
        () ->
            QuarkusTransaction.requiringNew()
                .run(
                    () -> {
                      try (Response ignored = storeResource.create(rolledBackStore)) {
                        throw new IllegalStateException("force rollback");
                      }
                    }));

    verifyNoInteractions(legacyStoreManagerGateway);
    assertEquals(0L, Store.count("name", rolledBackName));
  }

  @Test
  void shouldSynchronizeTheFinalManagedStateAfterUpdate() {
    Store original = store(uniqueName("before-update"));
    original.quantityProductsInStock = 3;
    try (Response ignored = storeResource.create(original)) {
      // Persist the fixture through the same public transaction boundary used in production.
    }
    clearInvocations(legacyStoreManagerGateway);

    Store update = store(uniqueName("after-update"));
    update.quantityProductsInStock = 8;

    Store result = storeResource.update(original.id, update);

    ArgumentCaptor<Store> committedStore = ArgumentCaptor.forClass(Store.class);
    verify(legacyStoreManagerGateway).updateStoreOnLegacySystem(committedStore.capture());
    assertAll(
        () -> assertEquals(original.id, result.id),
        () -> assertEquals(original.id, committedStore.getValue().id),
        () -> assertEquals(update.name, committedStore.getValue().name),
        () ->
            assertEquals(
                update.quantityProductsInStock,
                committedStore.getValue().quantityProductsInStock));
  }

  @Test
  void shouldNotSynchronizeOrPersistAnUpdateWhenTheTransactionRollsBack() {
    Store original = store(uniqueName("rollback-update-original"));
    String originalName = original.name;
    original.quantityProductsInStock = 4;
    try (Response ignored = storeResource.create(original)) {
      // Persist fixture.
    }
    clearInvocations(legacyStoreManagerGateway);

    Store update = store(uniqueName("rollback-update-new"));
    update.quantityProductsInStock = 9;

    assertThrows(
        IllegalStateException.class,
        () ->
            QuarkusTransaction.requiringNew()
                .run(
                    () -> {
                      storeResource.update(original.id, update);
                      throw new IllegalStateException("force rollback");
                    }));

    verifyNoInteractions(legacyStoreManagerGateway);
    Store persisted = Store.findById(original.id);
    assertAll(
        () -> assertEquals(originalName, persisted.name),
        () -> assertEquals(4, persisted.quantityProductsInStock));
  }

  @Test
  void shouldPatchOnlySuppliedFieldsAndAllowStockToBecomeZero() {
    Store original = store(uniqueName("patch"));
    original.quantityProductsInStock = 6;
    try (Response ignored = storeResource.create(original)) {
      // Persist the fixture through the same public transaction boundary used in production.
    }
    clearInvocations(legacyStoreManagerGateway);

    StoreResource.StorePatchRequest patch =
        new StoreResource.StorePatchRequest(null, 0);

    Store result = storeResource.patch(original.id, patch);

    ArgumentCaptor<Store> committedStore = ArgumentCaptor.forClass(Store.class);
    verify(legacyStoreManagerGateway).updateStoreOnLegacySystem(committedStore.capture());
    assertAll(
        () -> assertEquals(original.name, result.name),
        () -> assertEquals(0, result.quantityProductsInStock),
        () -> assertEquals(original.id, committedStore.getValue().id),
        () -> assertEquals(original.name, committedStore.getValue().name),
        () -> assertEquals(0, committedStore.getValue().quantityProductsInStock));
  }

  @Test
  void shouldNotSynchronizeOrPersistAPatchWhenTheTransactionRollsBack() {
    Store original = store(uniqueName("rollback-patch"));
    original.quantityProductsInStock = 6;
    try (Response ignored = storeResource.create(original)) {
      // Persist fixture.
    }
    clearInvocations(legacyStoreManagerGateway);

    assertThrows(
        IllegalStateException.class,
        () ->
            QuarkusTransaction.requiringNew()
                .run(
                    () -> {
                      storeResource.patch(
                          original.id, new StoreResource.StorePatchRequest(null, 0));
                      throw new IllegalStateException("force rollback");
                    }));

    verifyNoInteractions(legacyStoreManagerGateway);
    Store persisted = Store.findById(original.id);
    assertEquals(6, persisted.quantityProductsInStock);
  }

  private Store store(String name) {
    return new Store(name);
  }

  private String uniqueName(String prefix) {
    // Store names are capped at 40 characters by the entity mapping.
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
  }
}
