package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

/** Propagates committed store changes to the legacy integration. */
@ApplicationScoped
public class LegacyStoreSynchronizationListener {

  private final LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Inject
  public LegacyStoreSynchronizationListener(
      LegacyStoreManagerGateway legacyStoreManagerGateway) {
    this.legacyStoreManagerGateway = legacyStoreManagerGateway;
  }

  void synchronize(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) StoreChangedEvent event) {
    Store snapshot = toLegacyGatewayModel(event);

    switch (event.operation()) {
      case CREATED -> legacyStoreManagerGateway.createStoreOnLegacySystem(snapshot);
      case UPDATED -> legacyStoreManagerGateway.updateStoreOnLegacySystem(snapshot);
    }
  }

  private Store toLegacyGatewayModel(StoreChangedEvent event) {
    /*
     * LegacyStoreManagerGateway predates the event design and accepts Store. Reconstructing a
     * detached value here preserves that API without leaking the managed entity across the commit
     * boundary.
     */
    Store snapshot = new Store();
    snapshot.id = event.id();
    snapshot.name = event.name();
    snapshot.quantityProductsInStock = event.quantityProductsInStock();
    return snapshot;
  }
}
