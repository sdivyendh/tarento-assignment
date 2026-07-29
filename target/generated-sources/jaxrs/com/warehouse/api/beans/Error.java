
package com.warehouse.api.beans;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "code",
    "error"
})
@Generated("jsonschema2pojo")
public class Error {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    private Integer code;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("error")
    private String error;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    public Integer getCode() {
        return code;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("error")
    public String getError() {
        return error;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("error")
    public void setError(String error) {
        this.error = error;
    }

}
