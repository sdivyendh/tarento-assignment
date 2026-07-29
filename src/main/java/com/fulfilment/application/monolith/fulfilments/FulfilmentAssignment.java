package com.fulfilment.application.monolith.fulfilments;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "fulfilment_assignment",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_fulfilment_store_product_warehouse",
            columnNames = {"store_id", "product_id", "warehouse_id"}),
    indexes = {
      @Index(name = "idx_fulfilment_store", columnList = "store_id"),
      @Index(name = "idx_fulfilment_product", columnList = "product_id"),
      @Index(name = "idx_fulfilment_warehouse", columnList = "warehouse_id")
    })
@SequenceGenerator(
    name = "fulfilment_assignment_generator",
    sequenceName = "fulfilment_assignment_SEQ",
    allocationSize = 50)
public class FulfilmentAssignment {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fulfilment_assignment_generator")
  public Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false)
  public Store store;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  public Product product;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "warehouse_id", nullable = false)
  public DbWarehouse warehouse;

  @jakarta.persistence.Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt;
}
