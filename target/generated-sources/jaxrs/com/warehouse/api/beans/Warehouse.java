
package com.warehouse.api.beans;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "businessUnitCode",
    "location",
    "capacity",
    "stock"
})
@Generated("jsonschema2pojo")
public class Warehouse {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("businessUnitCode")
    private String businessUnitCode;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("location")
    private String location;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("capacity")
    private Integer capacity;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stock")
    private Integer stock;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("businessUnitCode")
    public String getBusinessUnitCode() {
        return businessUnitCode;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("businessUnitCode")
    public void setBusinessUnitCode(String businessUnitCode) {
        this.businessUnitCode = businessUnitCode;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("location")
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("capacity")
    public Integer getCapacity() {
        return capacity;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("capacity")
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stock")
    public Integer getStock() {
        return stock;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("stock")
    public void setStock(Integer stock) {
        this.stock = stock;
    }

}
