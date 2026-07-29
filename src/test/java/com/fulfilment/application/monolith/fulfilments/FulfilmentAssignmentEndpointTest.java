package com.fulfilment.application.monolith.fulfilments;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfilmentAssignmentEndpointTest {

  private static final String PATH = "/fulfilment-assignments";

  @Test
  void createsFiltersRejectsDuplicatesAndDeletesAnAssignment() {
    Response created =
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("storeId", 3, "productId", 3, "warehouseId", 3))
            .when()
            .post(PATH)
            .then()
            .statusCode(201)
            .body("storeName", equalTo("BESTÅ"))
            .body("productName", equalTo("BESTÅ"))
            .body("warehouseBusinessUnitCode", equalTo("MWH.023"))
            .extract()
            .response();
    Integer id = created.path("id");
    org.junit.jupiter.api.Assertions.assertTrue(
        created.header("Location").endsWith(PATH + "/" + id));

    given()
        .queryParam("storeId", 3)
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body("id", hasItem(id));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("storeId", 3, "productId", 3, "warehouseId", 3))
        .when()
        .post(PATH)
        .then()
        .statusCode(409)
        .body("code", equalTo(409));

    given().when().delete(PATH + "/" + id).then().statusCode(204);
    given().when().delete(PATH + "/" + id).then().statusCode(404);
  }
}
