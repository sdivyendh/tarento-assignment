package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

class LocationGatewayTest {

  @Test
  void shouldResolveKnownLocationWithItsConstraints() {
    LocationGateway locationGateway = new LocationGateway();

    Location location = locationGateway.resolveByIdentifier("ZWOLLE-002");

    assertNotNull(location);
    assertAll(
        () -> assertEquals("ZWOLLE-002", location.identification),
        () -> assertEquals(2, location.maxNumberOfWarehouses),
        () -> assertEquals(50, location.maxCapacity));
  }

  @Test
  void shouldReturnNullForUnknownIdentifier() {
    LocationGateway locationGateway = new LocationGateway();

    assertAll(
        () -> assertNull(locationGateway.resolveByIdentifier("UNKNOWN-001")),
        () -> assertNull(locationGateway.resolveByIdentifier("zwolle-002")),
        () -> assertNull(locationGateway.resolveByIdentifier(" ZWOLLE-002")));
  }

  @Test
  void shouldReturnNullForNullIdentifier() {
    LocationGateway locationGateway = new LocationGateway();

    assertNull(locationGateway.resolveByIdentifier(null));
  }

  @Test
  void shouldReturnNullForBlankIdentifier() {
    LocationGateway locationGateway = new LocationGateway();

    assertNull(locationGateway.resolveByIdentifier("   "));
  }
}
