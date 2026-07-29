package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseEndpointTest {

  private static final String PATH = "/warehouse";

  @Inject WarehouseRepository warehouseRepository;

  @Test
  void listsAndRetrievesSeededActiveWarehouses() {
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(
            "businessUnitCode",
            hasItems("MWH.001", "MWH.012", "MWH.023"));

    given()
        .when()
        .get(PATH + "/1")
        .then()
        .statusCode(200)
        .body("id", equalTo("1"))
        .body("businessUnitCode", equalTo("MWH.001"));
  }

  @Test
  void createsAndRetrievesAWarehouse() {
    Response created =
        createWarehouse("MWH.ENDPOINT.CREATE", "ZWOLLE-002", 20, 5);

    String id = created.path("id");
    assertNotNull(id);

    given()
        .when()
        .get(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("MWH.ENDPOINT.CREATE"))
        .body("location", equalTo("ZWOLLE-002"))
        .body("capacity", equalTo(20))
        .body("stock", equalTo(5));
  }

  @Test
  void archivesWithoutDeletingWarehouseHistory() {
    Response created =
        createWarehouse("MWH.ENDPOINT.ARCHIVE", "VETSBY-001", 20, 5);
    String id = created.path("id");

    given().when().delete(PATH + "/" + id).then().statusCode(204);
    given().when().delete(PATH + "/" + id).then().statusCode(404);

    given()
        .when()
        .get(PATH + "/" + id)
        .then()
        .statusCode(404)
        .body("code", equalTo(404));

    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body("id", not(hasItems(id)));

    assertNotNull(warehouseRepository.findById(Long.valueOf(id)).archivedAt);
  }

  @Test
  void atomicallyReplacesAnActiveWarehouse() {
    String businessUnitCode = "MWH.ENDPOINT.REPLACE";
    Response original =
        createWarehouse(businessUnitCode, "EINDHOVEN-001", 30, 10);
    String originalId = original.path("id");

    Response replacement =
        given()
            .contentType(ContentType.JSON)
            .body(
                warehouseRequest(
                    businessUnitCode, "EINDHOVEN-001", 35, 10))
            .when()
            .post(PATH + "/" + businessUnitCode + "/replacement")
            .then()
            .statusCode(200)
            .body("businessUnitCode", equalTo(businessUnitCode))
            .body("capacity", equalTo(35))
            .body("stock", equalTo(10))
            .extract()
            .response();

    String replacementId = replacement.path("id");
    assertNotNull(replacementId);
    assertNotEquals(originalId, replacementId);

    given().when().get(PATH + "/" + originalId).then().statusCode(404);
    given().when().get(PATH + "/" + replacementId).then().statusCode(200);
    assertNotNull(warehouseRepository.findById(Long.valueOf(originalId)).archivedAt);
  }

  @Test
  void returnsContractErrorsForInvalidAndMissingWarehouses() {
    given()
        .contentType(ContentType.JSON)
        .body(warehouseRequest("MWH.INVALID.LOCATION", "UNKNOWN-001", 20, 5))
        .when()
        .post(PATH)
        .then()
        .statusCode(400)
        .body("code", equalTo(400));

    given().when().get(PATH + "/not-a-number").then().statusCode(400);
    given().when().get(PATH + "/-1").then().statusCode(400);
    given().when().get(PATH + "/999999").then().statusCode(404);

    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "id", "99",
                "businessUnitCode", "MWH.INVALID.ID",
                "location", "AMSTERDAM-002",
                "capacity", 20,
                "stock", 5))
        .when()
        .post(PATH + "/MWH.INVALID.ID/replacement")
        .then()
        .statusCode(400);
  }

  @Test
  void rejectsDuplicateAndInvalidReplacementWithoutChangingTheActiveWarehouse() {
    String duplicateCode = "MWH.ENDPOINT.DUPLICATE";
    createWarehouse(duplicateCode, "HELMOND-001", 20, 5);

    given()
        .contentType(ContentType.JSON)
        .body(warehouseRequest(duplicateCode, "HELMOND-001", 20, 5))
        .when()
        .post(PATH)
        .then()
        .statusCode(400)
        .body(
            "error",
            equalTo(
                "An active warehouse with business unit code '"
                    + duplicateCode
                    + "' already exists"));

    String replacementCode = "MWH.ENDPOINT.INVALID.REPLACE";
    Response original =
        createWarehouse(replacementCode, "AMSTERDAM-002", 20, 5);
    String originalId = original.path("id");

    given()
        .contentType(ContentType.JSON)
        .body(warehouseRequest(replacementCode, "AMSTERDAM-002", 20, 4))
        .when()
        .post(PATH + "/" + replacementCode + "/replacement")
        .then()
        .statusCode(400);

    given()
        .when()
        .get(PATH + "/" + originalId)
        .then()
        .statusCode(200)
        .body("stock", equalTo(5));

    given()
        .contentType(ContentType.JSON)
        .body(warehouseRequest("MWH.DIFFERENT", "AMSTERDAM-002", 20, 5))
        .when()
        .post(PATH + "/" + replacementCode + "/replacement")
        .then()
        .statusCode(400);

    given()
        .contentType(ContentType.JSON)
        .body(warehouseRequest("MWH.DOES.NOT.EXIST", "AMSTERDAM-002", 20, 5))
        .when()
        .post(PATH + "/MWH.DOES.NOT.EXIST/replacement")
        .then()
        .statusCode(404);
  }

  private Response createWarehouse(
      String businessUnitCode, String location, int capacity, int stock) {
    return given()
        .contentType(ContentType.JSON)
        .body(warehouseRequest(businessUnitCode, location, capacity, stock))
        .when()
        .post(PATH)
        .then()
        .statusCode(201)
        .body("businessUnitCode", equalTo(businessUnitCode))
        .extract()
        .response();
  }

  private Map<String, Object> warehouseRequest(
      String businessUnitCode, String location, int capacity, int stock) {
    return Map.of(
        "businessUnitCode", businessUnitCode,
        "location", location,
        "capacity", capacity,
        "stock", stock);
  }
}
