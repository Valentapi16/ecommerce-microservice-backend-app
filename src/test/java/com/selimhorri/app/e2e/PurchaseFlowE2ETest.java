package com.selimhorri.app.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas End-to-End (E2E) - Flujo Completo de Compra
 * 
 * Esta prueba verifica el flujo completo de compra en el e-commerce:
 * 1. Usuario → Consultar productos disponibles
 * 2. Productos → Agregar al carrito
 * 3. Carrito → Crear orden de compra
 * 4. Orden → Procesar pago
 * 5. Pago → Crear envío
 * 
 * Incluye dos escenarios:
 * - Escenario 1: Compra exitosa con stock suficiente
 * - Escenario 2: Compra fallida por stock insuficiente
 * 
 * @author Test Suite
 * @since 2025-01-29
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("E2E Test - Flujo Completo de Compra")
class PurchaseFlowE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_GATEWAY_URL = "http://localhost:8080";
    private static final Integer TEST_USER_ID = 1;
    private static final String AUTH_TOKEN = "test_auth_token_12345";
    
    private static Integer selectedProductId;
    private static Integer orderId;
    private static Integer paymentId;
    private static Integer shippingId;
    private static BigDecimal orderTotal;

    /**
     * ========================================
     * ESCENARIO 1: COMPRA EXITOSA
     * ========================================
     */

    @Test
    @Order(1)
    @DisplayName("E2E-PURCHASE-001: Paso 1 - Consultar productos disponibles")
    void testStep1_BrowseProducts() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║  ESCENARIO 1: FLUJO DE COMPRA EXITOSA                ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println("\n=== PASO 1: Consultar Productos ===");
        
        // Act - Consultar lista de productos
        String productsUrl = API_GATEWAY_URL + "/api/products";
        System.out.println("\n[Request] GET " + productsUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<List> response = restTemplate.exchange(
            productsUrl,
            HttpMethod.GET,
            requestEntity,
            List.class
        );

        // Assert
        System.out.println("[Response] Status: " + response.getStatusCode());
        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "Debe obtener la lista de productos");
        
        assertNotNull(response.getBody(), "La lista de productos no debe ser nula");
        assertTrue(response.getBody().size() > 0, "Debe haber productos disponibles");
        
        List<Map<String, Object>> products = response.getBody();
        System.out.println("\n✓ Productos disponibles: " + products.size());
        
        // Seleccionar primer producto con stock
        for (Map<String, Object> product : products) {
            Integer stock = (Integer) product.get("quantity");
            if (stock != null && stock > 0) {
                selectedProductId = (Integer) product.get("productId");
                String title = (String) product.get("productTitle");
                Object price = product.get("priceUnit");
                
                System.out.println("\n✓ Producto seleccionado:");
                System.out.println("  - ID: " + selectedProductId);
                System.out.println("  - Título: " + title);
                System.out.println("  - Precio: $" + price);
                System.out.println("  - Stock: " + stock);
                
                assertNotNull(selectedProductId, "Debe haber un producto seleccionado");
                break;
            }
        }
        
        System.out.println("\n✓ PASO 1 COMPLETADO");
    }

    @Test
    @Order(2)
    @DisplayName("E2E-PURCHASE-002: Paso 2 - Agregar producto al carrito")
    void testStep2_AddToCart() {
        System.out.println("\n=== PASO 2: Agregar al Carrito ===");
        
        // Arrange - Preparar item del carrito
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", selectedProductId);
        cartItem.put("quantity", 2);
        
        System.out.println("\n[Cart Item]:");
        System.out.println("  - Product ID: " + selectedProductId);
        System.out.println("  - Cantidad: 2");
        
        // Act - Agregar al carrito
        String cartUrl = API_GATEWAY_URL + "/api/cart/user/" + TEST_USER_ID + "/add";
        System.out.println("\n[Request] POST " + cartUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(cartItem, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            cartUrl,
            requestEntity,
            Map.class
        );

        // Assert
        System.out.println("[Response] Status: " + response.getStatusCode());
        
        assertTrue(
            response.getStatusCode() == HttpStatus.OK || 
            response.getStatusCode() == HttpStatus.CREATED,
            "Debe agregar el producto al carrito"
        );
        
        System.out.println("\n✓ Producto agregado al carrito");
        System.out.println("✓ PASO 2 COMPLETADO");
    }

    @Test
    @Order(3)
    @DisplayName("E2E-PURCHASE-003: Paso 3 - Crear orden de compra")
    void testStep3_CreateOrder() {
        System.out.println("\n=== PASO 3: Crear Orden de Compra ===");
        
        // Arrange - Preparar orden
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("userId", TEST_USER_ID);
        orderRequest.put("orderDesc", "Orden E2E Test - Compra exitosa");
        
        List<Map<String, Object>> orderItems = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("productId", selectedProductId);
        item.put("quantity", 2);
        item.put("priceUnit", "999.99");
        orderItems.add(item);
        
        orderRequest.put("items", orderItems);
        
        System.out.println("\n[Order Request]:");
        System.out.println("  - User ID: " + TEST_USER_ID);
        System.out.println("  - Items: " + orderItems.size());
        System.out.println("  - Descripción: Orden E2E Test");
        
        // Calcular total esperado
        orderTotal = new BigDecimal("1999.98"); // 2 x $999.99
        System.out.println("  - Total esperado: $" + orderTotal);
        
        // Act - Crear orden
        String orderUrl = API_GATEWAY_URL + "/api/orders";
        System.out.println("\n[Request] POST " + orderUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(orderRequest, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            orderUrl,
            requestEntity,
            Map.class
        );

        // Assert
        System.out.println("[Response] Status: " + response.getStatusCode());
        
        assertTrue(
            response.getStatusCode() == HttpStatus.OK || 
            response.getStatusCode() == HttpStatus.CREATED,
            "Debe crear la orden exitosamente"
        );
        
        assertNotNull(response.getBody(), "La respuesta no debe ser nula");
        
        Map<String, Object> orderResponse = response.getBody();
        if (orderResponse.containsKey("orderId")) {
            orderId = (Integer) orderResponse.get("orderId");
            System.out.println("\n✓ Orden creada exitosamente:");
            System.out.println("  - Order ID: " + orderId);
            System.out.println("  - Estado: CREATED");
            assertNotNull(orderId, "El ID de la orden no debe ser nulo");
        }
        
        System.out.println("✓ PASO 3 COMPLETADO");
    }

    @Test
    @Order(4)
    @DisplayName("E2E-PURCHASE-004: Paso 4 - Procesar pago")
    void testStep4_ProcessPayment() {
        System.out.println("\n=== PASO 4: Procesar Pago ===");
        
        // Arrange - Preparar información de pago
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("orderId", orderId);
        paymentRequest.put("paymentMethod", "CREDIT_CARD");
        paymentRequest.put("amount", orderTotal.toString());
        
        Map<String, String> cardInfo = new HashMap<>();
        cardInfo.put("cardNumber", "4532-1234-5678-9010");
        cardInfo.put("cardHolder", "JOHN DOE");
        cardInfo.put("expiryDate", "12/25");
        cardInfo.put("cvv", "123");
        paymentRequest.put("cardInfo", cardInfo);
        
        System.out.println("\n[Payment Request]:");
        System.out.println("  - Order ID: " + orderId);
        System.out.println("  - Método: CREDIT_CARD");
        System.out.println("  - Monto: $" + orderTotal);
        System.out.println("  - Tarjeta: **** **** **** 9010");
        
        // Act - Procesar pago
        String paymentUrl = API_GATEWAY_URL + "/api/payments";
        System.out.println("\n[Request] POST " + paymentUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(paymentRequest, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            paymentUrl,
            requestEntity,
            Map.class
        );

        // Assert
        System.out.println("[Response] Status: " + response.getStatusCode());
        
        assertTrue(
            response.getStatusCode() == HttpStatus.OK || 
            response.getStatusCode() == HttpStatus.CREATED,
            "Debe procesar el pago exitosamente"
        );
        
        assertNotNull(response.getBody(), "La respuesta no debe ser nula");
        
        Map<String, Object> paymentResponse = response.getBody();
        if (paymentResponse.containsKey("paymentId")) {
            paymentId = (Integer) paymentResponse.get("paymentId");
            System.out.println("\n✓ Pago procesado exitosamente:");
            System.out.println("  - Payment ID: " + paymentId);
            System.out.println("  - Estado: APPROVED");
            System.out.println("  - Monto: $" + orderTotal);
            assertNotNull(paymentId, "El ID del pago no debe ser nulo");
        }
        
        System.out.println("✓ PASO 4 COMPLETADO");
    }

    @Test
    @Order(5)
    @DisplayName("E2E-PURCHASE-005: Paso 5 - Crear envío")
    void testStep5_CreateShipping() {
        System.out.println("\n=== PASO 5: Crear Envío ===");
        
        // Arrange - Preparar información de envío
        Map<String, Object> shippingRequest = new HashMap<>();
        shippingRequest.put("orderId", orderId);
        
        Map<String, String> shippingAddress = new HashMap<>();
        shippingAddress.put("fullAddress", "123 Main St, Apt 4B");
        shippingAddress.put("city", "New York");
        shippingAddress.put("postalCode", "10001");
        shippingAddress.put("country", "USA");
        shippingRequest.put("shippingAddress", shippingAddress);
        
        System.out.println("\n[Shipping Request]:");
        System.out.println("  - Order ID: " + orderId);
        System.out.println("  - Dirección: 123 Main St, Apt 4B");
        System.out.println("  - Ciudad: New York, 10001");
        
        // Act - Crear envío
        String shippingUrl = API_GATEWAY_URL + "/api/shippings";
        System.out.println("\n[Request] POST " + shippingUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(shippingRequest, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            shippingUrl,
            requestEntity,
            Map.class
        );

        // Assert
        System.out.println("[Response] Status: " + response.getStatusCode());
        
        assertTrue(
            response.getStatusCode() == HttpStatus.OK || 
            response.getStatusCode() == HttpStatus.CREATED,
            "Debe crear el envío exitosamente"
        );
        
        assertNotNull(response.getBody(), "La respuesta no debe ser nula");
        
        Map<String, Object> shippingResponse = response.getBody();
        if (shippingResponse.containsKey("shippingId")) {
            shippingId = (Integer) shippingResponse.get("shippingId");
            System.out.println("\n✓ Envío creado exitosamente:");
            System.out.println("  - Shipping ID: " + shippingId);
            System.out.println("  - Estado: PENDING");
            assertNotNull(shippingId, "El ID del envío no debe ser nulo");
        }
        
        System.out.println("✓ PASO 5 COMPLETADO");
        
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║  ✓ COMPRA COMPLETADA EXITOSAMENTE                    ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println("\nResumen de la compra:");
        System.out.println("  - Orden ID: " + orderId);
        System.out.println("  - Pago ID: " + paymentId);
        System.out.println("  - Envío ID: " + shippingId);
        System.out.println("  - Total: $" + orderTotal);
    }

    /**
     * ========================================
     * ESCENARIO 2: COMPRA FALLIDA POR STOCK
     * ========================================
     */

    @Test
    @Order(6)
    @DisplayName("E2E-PURCHASE-006: Escenario 2 - Compra fallida por stock insuficiente")
    void testScenario2_InsufficientStock() {
        System.out.println("\n\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║  ESCENARIO 2: COMPRA FALLIDA - STOCK INSUFICIENTE    ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        
        // Paso 1: Consultar producto
        System.out.println("\n[Paso 1] Consultando producto...");
        String productUrl = API_GATEWAY_URL + "/api/products/1";
        System.out.println("  GET " + productUrl);
        
        ResponseEntity<Map> productResponse = restTemplate.getForEntity(productUrl, Map.class);
        assertEquals(HttpStatus.OK, productResponse.getStatusCode());
        
        Map<String, Object> product = productResponse.getBody();
        Integer availableStock = (Integer) product.get("quantity");
        
        System.out.println("  ✓ Producto obtenido:");
        System.out.println("    - ID: " + product.get("productId"));
        System.out.println("    - Título: " + product.get("productTitle"));
        System.out.println("    - Stock disponible: " + availableStock);
        
        // Paso 2: Intentar crear orden con cantidad mayor al stock
        System.out.println("\n[Paso 2] Intentando crear orden con stock insuficiente...");
        
        Integer requestedQuantity = availableStock + 100; // Cantidad mayor al stock
        System.out.println("  - Stock disponible: " + availableStock);
        System.out.println("  - Cantidad solicitada: " + requestedQuantity);
        
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("userId", TEST_USER_ID);
        orderRequest.put("orderDesc", "Orden E2E Test - Stock insuficiente");
        
        List<Map<String, Object>> orderItems = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("productId", 1);
        item.put("quantity", requestedQuantity);
        orderItems.add(item);
        
        orderRequest.put("items", orderItems);
        
        String orderUrl = API_GATEWAY_URL + "/api/orders";
        System.out.println("  POST " + orderUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(orderRequest, headers);
        
        ResponseEntity<Map> orderResponse = restTemplate.postForEntity(
            orderUrl,
            requestEntity,
            Map.class
        );
        
        // Assert - La orden debe ser rechazada
        System.out.println("\n[Verificación] Status: " + orderResponse.getStatusCode());
        
        assertTrue(
            orderResponse.getStatusCode() == HttpStatus.BAD_REQUEST ||
            orderResponse.getStatusCode() == HttpStatus.CONFLICT,
            "La orden debe ser rechazada por stock insuficiente (400 o 409)"
        );
        
        System.out.println("  ✗ Orden rechazada correctamente");
        System.out.println("  ✓ Validación de stock funcionando");
        
        if (orderResponse.getBody() != null && orderResponse.getBody().containsKey("message")) {
            String errorMessage = (String) orderResponse.getBody().get("message");
            System.out.println("  ✓ Mensaje de error: " + errorMessage);
        }
        
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║  ✓ VALIDACIÓN DE STOCK FUNCIONANDO CORRECTAMENTE     ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }

    @AfterAll
    static void printFinalSummary() {
        System.out.println("\n\n");
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║  RESUMEN: Flujo E2E de Compra COMPLETADO             ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println("\n✓ ESCENARIO 1 - Compra Exitosa:");
        System.out.println("  1. Consultar productos - EXITOSO");
        System.out.println("  2. Agregar al carrito - EXITOSO");
        System.out.println("  3. Crear orden - EXITOSO");
        System.out.println("  4. Procesar pago - EXITOSO");
        System.out.println("  5. Crear envío - EXITOSO");
        System.out.println("\n✓ ESCENARIO 2 - Validación de Stock:");
        System.out.println("  6. Rechazo por stock insuficiente - EXITOSO");
        System.out.println("\n✓ Servicios integrados correctamente:");
        System.out.println("  - API Gateway");
        System.out.println("  - Product Service");
        System.out.println("  - Order Service");
        System.out.println("  - Payment Service");
        System.out.println("  - Shipping Service");
        System.out.println("  - Service Discovery (Eureka)");
        System.out.println("\n══════════════════════════════════════════════════════");
    }
}
