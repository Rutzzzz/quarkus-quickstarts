package org.acme.faulttolerance;

import static io.restassured.RestAssured.get;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringContains.containsString;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.DisabledOnNativeImage;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CoffeeResourceTest {

    @Inject
    private CoffeeResource coffeeResource;

    @Test
    @DisabledOnNativeImage("@Inject in tests doesn't work for native mode")
    public void testCoffeeList() {
        coffeeResource.resetCounter();
        coffeeResource.setFailRatio(0f);
        get("/coffee")
                .then()
                .statusCode(200)
                .body("id", hasItems(1, 2, 3))
                .body("countryOfOrigin", hasItems("Colombia", "Bolivia", "Vietnam"));
        Assertions.assertEquals(1, coffeeResource.getCounter().longValue());

        coffeeResource.resetCounter();
        coffeeResource.setFailRatio(1f);
        get("/coffee")
                .then()
                .statusCode(500);
        Assertions.assertEquals(5, coffeeResource.getCounter().longValue());
    }

    @Test
    @DisabledOnNativeImage("@Inject in tests doesn't work for native mode")
    public void testCoffeeDetail() {
        coffeeResource.setFailRatio(0f);
        get("/coffee/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("countryOfOrigin", is("Colombia"));

        coffeeResource.setFailRatio(1f);
        get("/coffee/1")
                .then()
                .statusCode(500);
    }

    @Test
    public void testAvailability() {
        get("/coffee/1/availability").then()
                .statusCode(200).body(is("20"));
        get("/coffee/1/availability").then()
                .statusCode(200).body(is("20"));
        get("/coffee/1/availability").then()
                .statusCode(500).body(is("RuntimeException: Service failed."));
        get("/coffee/1/availability").then()
                .statusCode(500).body(is("RuntimeException: Service failed."));
        get("/coffee/1/availability").then()
                .statusCode(500).body(containsString("CircuitBreakerOpenException"));
    }

    @Test
    public void testRecommendations() {
        get("/coffee/2/recommendations").then()
                .statusCode(200)
                .body("id", hasItem(1),
                        "countryOfOrigin", hasItem("Colombia"));
    }
}
