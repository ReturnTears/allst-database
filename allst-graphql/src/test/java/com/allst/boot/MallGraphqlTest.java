package com.allst.boot;

import com.allst.boot.order.enums.OrderStatus;
import com.allst.boot.order.service.OrderEventBus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
class MallGraphqlTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private OrderEventBus orderEventBus;

    @Test
    void health() {
        graphQlTester.document("{ health }")
                .execute()
                .path("health")
                .entity(String.class)
                .isEqualTo("ok");
    }

    @Test
    void productCrudAndNestedCategory() {
        String categoryName = "茶饮-" + System.nanoTime();
        String categoryId = mutateId("mutation($input: CategoryInput!) { createCategory(input: $input) { id } }",
                "createCategory.id", map("name", categoryName));
        String productId = mutateId("mutation($input: ProductInput!) { createProduct(input: $input) { id } }",
                "createProduct.id", productInput("明前龙井", categoryId, "10"));

        graphQlTester.document("query($id: ID!) { product(id: $id) { name inStock category { name productCount } } }")
                .variable("id", productId)
                .execute()
                .path("product.category.name")
                .entity(String.class)
                .isEqualTo(categoryName);

        graphQlTester.document("mutation($id: ID!, $input: ProductInput!) { updateProduct(id: $id, input: $input) { stock } }")
                .variable("id", productId)
                .variable("input", productInput("明前龙井", categoryId, "0"))
                .execute()
                .path("updateProduct.stock")
                .entity(Integer.class)
                .isEqualTo(0);

        graphQlTester.document("mutation($id: ID!) { deleteProduct(id: $id) }")
                .variable("id", productId)
                .execute()
                .path("deleteProduct")
                .entity(Boolean.class)
                .isEqualTo(true);
        graphQlTester.document("mutation($id: ID!) { deleteCategory(id: $id) }")
                .variable("id", categoryId)
                .execute()
                .path("deleteCategory")
                .entity(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void orderDeductsStockAndCancelRestoresIt() {
        String suffix = String.valueOf(System.nanoTime());
        String categoryId = mutateId("mutation($input: CategoryInput!) { createCategory(input: $input) { id } }",
                "createCategory.id", map("name", "器物-" + suffix));
        String productId = mutateId("mutation($input: ProductInput!) { createProduct(input: $input) { id } }",
                "createProduct.id", productInput("青瓷杯" + suffix, categoryId, "10"));
        String customerId = mutateId("mutation($input: CreateCustomerInput!) { createCustomer(input: $input) { id } }",
                "createCustomer.id", customerInput(suffix));
        String addressId = graphQlTester.document("query($id: ID!) { customer(id: $id) { addresses { id } } }")
                .variable("id", customerId)
                .execute()
                .path("customer.addresses[0].id")
                .entity(String.class)
                .get();

        Map<String, Object> line = map("productId", productId, "quantity", 3);
        Map<String, Object> orderInput = new LinkedHashMap<String, Object>();
        orderInput.put("customerId", customerId);
        orderInput.put("addressId", addressId);
        orderInput.put("remark", "轻放");
        orderInput.put("items", java.util.Collections.singletonList(line));
        String orderId = mutateId("mutation($input: CreateOrderInput!) { createOrder(input: $input) { id status totalAmount } }",
                "createOrder.id", orderInput);

        Integer stock = graphQlTester.document("query($id: ID!) { product(id: $id) { stock } }")
                .variable("id", productId)
                .execute()
                .path("product.stock")
                .entity(Integer.class)
                .get();
        assertEquals(7, stock.intValue());

        Long numericOrderId = Long.valueOf(orderId);
        StepVerifier.create(orderEventBus.stream().filter(order -> numericOrderId.equals(order.getId())).take(1))
                .then(() -> graphQlTester.document("mutation($id: ID!) { cancelOrder(id: $id) { status } }")
                        .variable("id", orderId)
                        .execute()
                        .path("cancelOrder.status")
                        .entity(OrderStatus.class)
                        .isEqualTo(OrderStatus.CANCELLED))
                .expectNextMatches(order -> order.getStatus() == OrderStatus.CANCELLED)
                .verifyComplete();

        Integer restored = graphQlTester.document("query($id: ID!) { product(id: $id) { stock } }")
                .variable("id", productId)
                .execute()
                .path("product.stock")
                .entity(Integer.class)
                .get();
        assertEquals(10, restored.intValue());

        graphQlTester.document("mutation($id: ID!) { deleteProduct(id: $id) }")
                .variable("id", productId)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertFalse(errors.isEmpty());
                    assertEquals("CONFLICT", errors.get(0).getExtensions().get("code"));
                });

        Boolean deleted = graphQlTester.document("mutation($id: ID!) { deleteOrder(id: $id) }")
                .variable("id", orderId)
                .execute()
                .path("deleteOrder")
                .entity(Boolean.class)
                .get();
        assertTrue(deleted);

        Boolean productDeleted = graphQlTester.document("mutation($id: ID!) { deleteProduct(id: $id) }")
                .variable("id", productId)
                .execute()
                .path("deleteProduct")
                .entity(Boolean.class)
                .get();
        assertTrue(productDeleted);
    }

    @Test
    void missingProductUsesNotFoundCode() {
        graphQlTester.document("query { product(id: 999999) { id } }")
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertFalse(errors.isEmpty());
                    assertEquals("NOT_FOUND", errors.get(0).getExtensions().get("code"));
                });
    }

    @Test
    void deepQueryIsRejected() {
        String nested = "name";
        for (int i = 0; i < 20; i++) {
            nested = "children { " + nested + " }";
        }
        String query = "{ categories { " + nested + " } }";
        graphQlTester.document(query)
                .execute()
                .errors()
                .satisfy(errors -> assertFalse(errors.isEmpty()));
    }

    private Map<String, Object> productInput(String name, String categoryId, String stock) {
        Map<String, Object> input = new LinkedHashMap<String, Object>();
        input.put("name", name);
        input.put("description", "测试");
        input.put("price", "128.00");
        input.put("stock", Integer.valueOf(stock));
        input.put("status", stock.equals("0") ? "OFF_SALE" : "ON_SALE");
        input.put("categoryId", categoryId);
        return input;
    }

    private Map<String, Object> customerInput(String suffix) {
        Map<String, Object> address = new LinkedHashMap<String, Object>();
        address.put("receiver", "林夏");
        address.put("phone", "13800001111");
        address.put("province", "浙江省");
        address.put("city", "杭州市");
        address.put("detail", "龙井路1号");
        address.put("defaultAddress", true);
        Map<String, Object> input = new LinkedHashMap<String, Object>();
        input.put("name", "林夏");
        input.put("email", "linxia-" + suffix + "@mall.test");
        input.put("phone", "13800001111");
        input.put("address", address);
        return input;
    }

    private String mutateId(String document, String path, Map<String, Object> input) {
        return graphQlTester.document(document)
                .variable("input", input)
                .execute()
                .path(path)
                .entity(String.class)
                .get();
    }

    private Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }
}
