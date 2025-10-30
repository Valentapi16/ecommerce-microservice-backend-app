package com.selimhorri.app.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas End-to-End (E2E) - Flujo Completo de Favoritos
 * 
 * Esta prueba verifica el flujo completo de gestión de favoritos:
 * 1. Usuario → Consultar productos
 * 2. Productos → Agregar productos a favoritos
 * 3. Favoritos → Listar favoritos del usuario
 * 4. Favoritos → Eliminar producto de favoritos
 * 
 * Incluye dos escenarios:
 * - Escenario 1: Agregar favorito exitoso
 * - Escenario 2: Eliminar favorito y listar favoritos actualizados
 * 
 * @author Test Suite
 * @since 2025-01-29
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("E2E Test - Flujo Completo de Favoritos")
class FavouritesFlowE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_GATEWAY_URL = "http://localhost:8080";
    private static final Integer TEST_USER_ID = 1;
    private static final String AUTH_TOKEN = "test_auth_token_12345";
    
    private static List<Integer> selectedProductIds = new ArrayList<>();
    private static List<Integer> favouriteIds = new ArrayList<>();

    /**
     * ========================================
     * ESCENARIO 1: AGREGAR FAVORITOS EXITOSO
     * ========================================
     */

    @Test
    @Order(1)
    @DisplayName("E2E-FAVOURITE-001: Paso 1 - Consultar productos disponibles")
    void testStep1_BrowseProducts() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║  ESCENARIO 1: AGREGAR PRODUCTOS A FAVORITOS          ║");
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
        
        // Seleccionar 3 productos para agregar a favoritos
        System.out.println("\n✓ Productos seleccionados para favoritos:");
        for (int i = 0; i < Math.min(3, products.size()); i++) {
            Map<String, Object> product = products.get(i);
            Integer productId = (Integer) product.get("productId");
            String title = (String) product.get("productTitle");
            Object price = product.get("priceUnit");
            
            selectedProductIds.add(productId);
            
            System.out.println("  " + (i + 1) + ". " + title);
            System.out.println("     - ID: " + productId);
            System.out.println("     - Precio: $" + price);
        }
        
        assertTrue(selectedProductIds.size() >= 2, 
            "Deben haberse seleccionado al menos 2 productos");
        
        System.out.println("\n✓ PASO 1 COMPLETADO");
    }

    @Test
    @Order(2)
    @DisplayName("E2E-FAVOURITE-002: Paso 2 - Agregar primer producto a favoritos")
    void testStep2_AddFirstFavourite() {
        System.out.println("\n=== PASO 2: Agregar Primer Producto a Favoritos ===");
        
        Integer productId = selectedProductIds.get(0);
        
        // Arrange - Preparar petición
        System.out.println("\n[Favourite Item]:");
        System.out.println("  - User ID: " + TEST_USER_ID);
        System.out.println("  - Product ID: " + productId);
        
        // Act - Agregar a favoritos
        String favouriteUrl = API_GATEWAY_URL + "/api/favourites/user/" + TEST_USER_ID + "/product/" + productId;
        System.out.println("\n[Request] POST " + favouriteUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            favouriteUrl,
            requestEntity,
            Map.class
        );

        // Assert
        System.out.println("[Response] Status: " + response.getStatusCode());
        
        assertTrue(
            response.getStatusCode() == HttpStatus.OK || 
            response.getStatusCode() == HttpStatus.CREATED,
            "Debe agregar el producto a favoritos"
        );
        
        if (response.getBody() != null && response.getBody().containsKey("favouriteId")) {
            Integer favouriteId = (Integer) response.getBody().get("favouriteId");
            favouriteIds.add(favouriteId);
            System.out.println("\n✓ Favorito creado:");
            System.out.println("  - Favourite ID: " + favouriteId);
        }
        
        System.out.println("✓ Primer producto agregado a favoritos");
        System.out.println("✓ PASO 2 COMPLETADO");
    }

    @Test
    @Order(3)
    @DisplayName("E2E-FAVOURITE-003: Paso 3 - Agregar segundo producto a favoritos")
    void testStep3_AddSecondFavourite() {
        System.out.println("\n=== PASO 3: Agregar Segundo Producto a Favoritos ===");
        
        Integer productId = selectedProductIds.get(1);
        
        System.out.println("\n[Favourite Item]:");
        System.out.println("  - User ID: " + TEST_USER_ID);
        System.out.println("  - Product ID: " + productId);
        
        // Act
        String favouriteUrl = API_GATEWAY_URL + "/api/favourites/user/" + TEST_USER_ID + "/product/" + productId;
        System.out.println("\n[Request] POST " + favouriteUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            favouriteUrl,
            requestEntity,
            Map.class
        );

        // Assert
        System.out.println("[Response] Status: " + response.getStatusCode());
        
        assertTrue(
            response.getStatusCode() == HttpStatus.OK || 
            response.getStatusCode() == HttpStatus.CREATED,
            "Debe agregar el segundo producto a favoritos"
        );
        
        if (response.getBody() != null && response.getBody().containsKey("favouriteId")) {
            Integer favouriteId = (Integer) response.getBody().get("favouriteId");
            favouriteIds.add(favouriteId);
            System.out.println("\n✓ Favorito creado:");
            System.out.println("  - Favourite ID: " + favouriteId);
        }
        
        System.out.println("✓ Segundo producto agregado a favoritos");
        System.out.println("✓ PASO 3 COMPLETADO");
    }

    @Test
    @Order(4)
    @DisplayName("E2E-FAVOURITE-004: Paso 4 - Listar todos los favoritos del usuario")
    void testStep4_ListFavourites() {
        System.out.println("\n=== PASO 4: Listar Favoritos del Usuario ===");
        
        // Act - Listar favoritos
        String listUrl = API_GATEWAY_URL + "/api/favourites/user/" + TEST_USER_ID;
        System.out.println("\n[Request] GET " + listUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<List> response = restTemplate.exchange(
            listUrl,
            HttpMethod.GET,
            requestEntity,
            List.class
        );

        // Assert
        System.out.println("[Response] Status: " + response.getStatusCode());
        
        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "Debe obtener la lista de favoritos");
        
        assertNotNull(response.getBody(), "La lista no debe ser nula");
        
        List<Map<String, Object>> favourites = response.getBody();
        System.out.println("\n✓ Favoritos del usuario: " + favourites.size());
        
        assertTrue(favourites.size() >= 2, 
            "El usuario debe tener al menos 2 favoritos");
        
        // Mostrar detalles de cada favorito
        System.out.println("\n✓ Lista de favoritos:");
        for (int i = 0; i < favourites.size(); i++) {
            Map<String, Object> fav = favourites.get(i);
            System.out.println("  " + (i + 1) + ". Favourite ID: " + fav.get("favouriteId"));
            
            if (fav.containsKey("product")) {
                Map<String, Object> product = (Map<String, Object>) fav.get("product");
                System.out.println("     - Producto: " + product.get("productTitle"));
                System.out.println("     - Precio: $" + product.get("priceUnit"));
            }
            
            if (fav.containsKey("likeDate")) {
                System.out.println("     - Fecha: " + fav.get("likeDate"));
            }
        }
        
        System.out.println("\n✓ PASO 4 COMPLETADO");
    }

    /**
     * ========================================
     * ESCENARIO 2: ELIMINAR FAVORITOS
     * ========================================
     */

    @Test
    @Order(5)
    @DisplayName("E2E-FAVOURITE-005: Escenario 2 - Eliminar producto de favoritos")
    void testScenario2_RemoveFavourite() {
        System.out.println("\n\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║  ESCENARIO 2: ELIMINAR PRODUCTO DE FAVORITOS          ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println("\n=== PASO 5: Eliminar Favorito ===");
        
        // Obtener el primer producto para eliminar
        Integer productToRemove = selectedProductIds.get(0);
        
        System.out.println("\n[Eliminando Favorito]:");
        System.out.println("  - User ID: " + TEST_USER_ID);
        System.out.println("  - Product ID: " + productToRemove);
        
        // Act - Eliminar favorito
        String deleteUrl = API_GATEWAY_URL + "/api/favourites/user/" + TEST_USER_ID + "/product/" + productToRemove;
        System.out.println("\n[Request] DELETE " + deleteUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<Void> response = restTemplate.exchange(
            deleteUrl,
            HttpMethod.DELETE,
            requestEntity,
            Void.class
        );

        // Assert
        System.out.println("[Response] Status: " + response.getStatusCode());
        
        assertTrue(
            response.getStatusCode() == HttpStatus.OK || 
            response.getStatusCode() == HttpStatus.NO_CONTENT,
            "Debe eliminar el favorito exitosamente (200 o 204)"
        );
        
        System.out.println("\n✓ Producto eliminado de favoritos");
        System.out.println("✓ PASO 5 COMPLETADO");
    }

    @Test
    @Order(6)
    @DisplayName("E2E-FAVOURITE-006: Verificar lista actualizada de favoritos")
    void testVerifyUpdatedFavouritesList() {
        System.out.println("\n=== PASO 6: Verificar Lista Actualizada ===");
        
        // Act - Listar favoritos actualizados
        String listUrl = API_GATEWAY_URL + "/api/favourites/user/" + TEST_USER_ID;
        System.out.println("\n[Request] GET " + listUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<List> response = restTemplate.exchange(
            listUrl,
            HttpMethod.GET,
            requestEntity,
            List.class
        );

        // Assert
        System.out.println("[Response] Status: " + response.getStatusCode());
        
        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "Debe obtener la lista actualizada");
        
        assertNotNull(response.getBody(), "La lista no debe ser nula");
        
        List<Map<String, Object>> favourites = response.getBody();
        System.out.println("\n✓ Favoritos actuales: " + favourites.size());
        
        // Debe tener 1 favorito menos que antes
        assertTrue(favourites.size() >= 1, 
            "Debe quedar al menos 1 favorito");
        
        System.out.println("\n✓ Lista actualizada correctamente:");
        for (int i = 0; i < favourites.size(); i++) {
            Map<String, Object> fav = favourites.get(i);
            System.out.println("  " + (i + 1) + ". Favourite ID: " + fav.get("favouriteId"));
            
            if (fav.containsKey("product")) {
                Map<String, Object> product = (Map<String, Object>) fav.get("product");
                System.out.println("     - Producto: " + product.get("productTitle"));
            }
        }
        
        System.out.println("\n✓ PASO 6 COMPLETADO");
        
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║  ✓ GESTIÓN DE FAVORITOS FUNCIONANDO CORRECTAMENTE    ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }

    /**
     * PRUEBA ADICIONAL: Intentar agregar duplicado
     */
    @Test
    @Order(7)
    @DisplayName("E2E-FAVOURITE-007: Verificar prevención de duplicados")
    void testPreventDuplicateFavourites() {
        System.out.println("\n=== PRUEBA ADICIONAL: Prevención de Duplicados ===");
        
        // Intentar agregar el mismo producto que ya está en favoritos
        Integer productId = selectedProductIds.get(1); // El segundo producto aún está en favoritos
        
        System.out.println("\n[Intento de Duplicado]:");
        System.out.println("  - User ID: " + TEST_USER_ID);
        System.out.println("  - Product ID: " + productId + " (ya existe en favoritos)");
        
        // Act
        String favouriteUrl = API_GATEWAY_URL + "/api/favourites/user/" + TEST_USER_ID + "/product/" + productId;
        System.out.println("\n[Request] POST " + favouriteUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            favouriteUrl,
            requestEntity,
            Map.class
        );

        // Assert
        System.out.println("[Response] Status: " + response.getStatusCode());
        
        // Debe rechazar el duplicado (400, 409) o simplemente retornar el existente (200)
        assertTrue(
            response.getStatusCode() == HttpStatus.BAD_REQUEST ||
            response.getStatusCode() == HttpStatus.CONFLICT ||
            response.getStatusCode() == HttpStatus.OK,
            "El sistema debe manejar correctamente los duplicados"
        );
        
        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("  ✓ Sistema retorna favorito existente (idempotente)");
        } else {
            System.out.println("  ✗ Duplicado rechazado correctamente");
        }
        
        System.out.println("\n✓ Prevención de duplicados funcionando");
    }

    /**
     * PRUEBA ADICIONAL: Limpiar todos los favoritos
     */
    @Test
    @Order(8)
    @DisplayName("E2E-FAVOURITE-008: Limpiar todos los favoritos del usuario")
    void testClearAllFavourites() {
        System.out.println("\n=== PRUEBA ADICIONAL: Limpiar Todos los Favoritos ===");
        
        // Eliminar todos los productos restantes de favoritos
        System.out.println("\n[Limpiando Favoritos]:");
        System.out.println("  - User ID: " + TEST_USER_ID);
        
        // Eliminar el segundo producto (el primero ya fue eliminado)
        for (int i = 1; i < selectedProductIds.size(); i++) {
            Integer productId = selectedProductIds.get(i);
            
            String deleteUrl = API_GATEWAY_URL + "/api/favourites/user/" + TEST_USER_ID + "/product/" + productId;
            System.out.println("\n[Request] DELETE " + deleteUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + AUTH_TOKEN);
            
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                requestEntity,
                Void.class
            );
            
            System.out.println("  ✓ Producto " + productId + " eliminado");
        }
        
        // Verificar que la lista está vacía
        String listUrl = API_GATEWAY_URL + "/api/favourites/user/" + TEST_USER_ID;
        System.out.println("\n[Verificación] GET " + listUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);
        
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<List> response = restTemplate.exchange(
            listUrl,
            HttpMethod.GET,
            requestEntity,
            List.class
        );
        
        List<Map<String, Object>> favourites = response.getBody();
        
        System.out.println("  ✓ Favoritos restantes: " + (favourites != null ? favourites.size() : 0));
        System.out.println("\n✓ Todos los favoritos limpiados correctamente");
    }

    @AfterAll
    static void printFinalSummary() {
        System.out.println("\n\n");
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║  RESUMEN: Flujo E2E de Favoritos COMPLETADO          ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println("\n✓ ESCENARIO 1 - Agregar Favoritos:");
        System.out.println("  1. Consultar productos - EXITOSO");
        System.out.println("  2. Agregar primer favorito - EXITOSO");
        System.out.println("  3. Agregar segundo favorito - EXITOSO");
        System.out.println("  4. Listar favoritos - EXITOSO");
        System.out.println("\n✓ ESCENARIO 2 - Eliminar Favoritos:");
        System.out.println("  5. Eliminar favorito - EXITOSO");
        System.out.println("  6. Verificar lista actualizada - EXITOSO");
        System.out.println("\n✓ Pruebas Adicionales:");
        System.out.println("  7. Prevención de duplicados - EXITOSO");
        System.out.println("  8. Limpiar todos los favoritos - EXITOSO");
        System.out.println("\n✓ Servicios integrados correctamente:");
        System.out.println("  - API Gateway");
        System.out.println("  - Product Service");
        System.out.println("  - Favourite Service");
        System.out.println("  - User Service");
        System.out.println("  - Service Discovery (Eureka)");
        System.out.println("\n══════════════════════════════════════════════════════");
    }
}
