package com.fulfilment.application.monolith.warehouses.domain.models;

public class Location {
  public final String identification;

  // maximum number of warehouses that can be created in this location
  public final int maxNumberOfWarehouses;

  // maximum capacity of the location summing all the warehouse capacities
  public final int maxCapacity;

  public Location(String identification, int maxNumberOfWarehouses, int maxCapacity) {
    this.identification = identification;
    this.maxNumberOfWarehouses = maxNumberOfWarehouses;
    this.maxCapacity = maxCapacity;
  }
}
