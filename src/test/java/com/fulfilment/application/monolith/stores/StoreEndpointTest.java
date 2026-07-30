package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreEndpointTest {

  private static final String PATH = "/store";

  @Test
  void createsListsGetsUpdatesPatchesAndDeletesAStore() {
    String originalName = uniqueName("store");
    Response created =
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("name", originalName, "quantityProductsInStock", 6))
            .when()
            .post(PATH)
            .then()
            .statusCode(201)
            .body("name", equalTo(originalName))
            .extract()
            .response();
    Number id = created.path("id");

    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body("name", hasItem(originalName));

    given()
        .when()
        .get(PATH + "/" + id.longValue())
        .then()
        .statusCode(200)
        .body("quantityProductsInStock", equalTo(6));

    String updatedName = uniqueName("updated-store");
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("name", updatedName, "quantityProductsInStock", 11))
        .when()
        .put(PATH + "/" + id.longValue())
        .then()
        .statusCode(200)
        .body("name", equalTo(updatedName))
        .body("quantityProductsInStock", equalTo(11));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("quantityProductsInStock", 0))
        .when()
        .patch(PATH + "/" + id.longValue())
        .then()
        .statusCode(200)
        .body("name", equalTo(updatedName))
        .body("quantityProductsInStock", equalTo(0));

    given().when().delete(PATH + "/" + id.longValue()).then().statusCode(204);
    given().when().get(PATH + "/" + id.longValue()).then().statusCode(404);
  }

  @Test
  void rejectsInvalidStoreRequestsAndMissingStores() {
    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "id", 99,
                "name", uniqueName("invalid-store"),
                "quantityProductsInStock", 1))
        .when()
        .post(PATH)
        .then()
        .statusCode(422)
        .body("code", equalTo(422));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("quantityProductsInStock", 1))
        .when()
        .put(PATH + "/999999")
        .then()
        .statusCode(422)
        .body("error", equalTo("Store Name was not set on request."));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("name", uniqueName("missing-store"), "quantityProductsInStock", 1))
        .when()
        .put(PATH + "/999999")
        .then()
        .statusCode(404);

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("quantityProductsInStock", 1))
        .when()
        .patch(PATH + "/999999")
        .then()
        .statusCode(404);

    given()
        .contentType(ContentType.JSON)
        .body(Map.of())
        .when()
        .patch(PATH + "/999999")
        .then()
        .statusCode(422);

    given().when().delete(PATH + "/999999").then().statusCode(404);
  }

  private String uniqueName(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
  }
}
