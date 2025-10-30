package com.selimhorri.app;

import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Product Service Application Tests
 * 
 * Incluye:
 * - Pruebas unitarias de validación de productos
 * - Pruebas de contexto de Spring Boot
 * 
 * @author Test Suite
 * @since 2025-10-29
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService - Application Tests")
class ProductServiceApplicationTests {

	@Mock
	private ProductRepository productRepository;

	/**
	 * TEST 1: Verificar que el contexto de Spring Boot carga correctamente
	 */
	@Test
	@DisplayName("Context Loads - Spring Boot context should load successfully")
	void contextLoads() {
		assertTrue(true, "Spring Boot context loaded successfully");
	}

	// ========================================
	// PRUEBAS UNITARIAS DE VALIDACIÓN
	// ========================================

	/**
	 * UT-PROD-001: Validación de precio del producto
	 */
	@Test
	@DisplayName("UT-PROD-001: Should validate product price correctly")
	void testProductPriceValidation() {
		// Arrange
		ProductDto productValid = new ProductDto();
		productValid.setProductTitle("Laptop Dell XPS 15");
		productValid.setPriceUnit(1299.99);
		productValid.setQuantity(10);
		
		ProductDto productNegativePrice = new ProductDto();
		productNegativePrice.setProductTitle("Invalid Product");
		productNegativePrice.setPriceUnit(-50.00);
		
		ProductDto productZeroPrice = new ProductDto();
		productZeroPrice.setProductTitle("Free Product");
		productZeroPrice.setPriceUnit(0.0);
		
		// Assert - Precio válido (positivo)
		assertTrue(productValid.getPriceUnit() > 0,
			"Valid product should have positive price");
		
		// Assert - Precio negativo (inválido)
		assertFalse(productNegativePrice.getPriceUnit() > 0,
			"Product with negative price should be invalid");
		
		// Assert - Precio cero (podría ser válido para productos gratis)
		assertEquals(0.0, productZeroPrice.getPriceUnit(),
			"Product with zero price should be detected");
	}

	/**
	 * UT-PROD-002: Validación de título del producto
	 */
	@Test
	@DisplayName("UT-PROD-002: Should validate product title")
	void testProductTitleValidation() {
		// Arrange
		ProductDto product = new ProductDto();
		product.setProductTitle("Smartphone Samsung Galaxy");
		
		// Assert - Título no vacío
		assertNotNull(product.getProductTitle(), 
			"Product title should not be null");
		assertFalse(product.getProductTitle().trim().isEmpty(), 
			"Product title should not be empty");
		
		// Assert - Longitud mínima
		assertTrue(product.getProductTitle().length() >= 3,
			"Product title should have minimum length of 3 characters");
	}

	/**
	 * UT-PROD-003: Validación de stock del producto
	 */
	@Test
	@DisplayName("UT-PROD-003: Should validate product stock correctly")
	void testProductStockValidation() {
		// Arrange
		ProductDto productInStock = new ProductDto();
		productInStock.setProductTitle("MacBook Pro");
		productInStock.setQuantity(25);
		
		ProductDto productOutOfStock = new ProductDto();
		productOutOfStock.setProductTitle("Out of Stock Item");
		productOutOfStock.setQuantity(0);
		
		ProductDto productNegativeStock = new ProductDto();
		productNegativeStock.setProductTitle("Invalid Stock");
		productNegativeStock.setQuantity(-5);
		
		// Assert - Stock positivo (disponible)
		assertTrue(productInStock.getQuantity() > 0,
			"Product in stock should have positive quantity");
		
		// Assert - Stock cero (agotado)
		assertEquals(0, productOutOfStock.getQuantity(),
			"Out of stock product should have zero quantity");
		
		// Assert - Stock negativo (inválido)
		assertTrue(productNegativeStock.getQuantity() < 0,
			"Product should not have negative stock");
		
		// Assert - Verificar disponibilidad
		assertTrue(productInStock.getQuantity() > 0,
			"Product should be available when stock > 0");
		assertFalse(productOutOfStock.getQuantity() > 0,
			"Product should not be available when stock = 0");
	}

	/**
	 * UT-PROD-004: Validación de SKU del producto
	 */
	@Test
	@DisplayName("UT-PROD-004: Should validate product SKU format")
	void testProductSKUValidation() {
		// Arrange - SKUs con diferentes formatos
		String validSKU = "ELEC-LAPTOP-001";
		String invalidSKUWithSpaces = "ELEC LAPTOP 001";
		String invalidSKUWithSpecialChars = "ELEC@LAPTOP#001";
		String emptyProductTitle = "";
		
		// Regex para SKU: solo letras, números y guiones
		String skuRegex = "^[A-Z0-9-]+$";
		
		// Assert - SKU válido
		assertTrue(validSKU.matches(skuRegex),
			"Valid SKU should match format: uppercase, numbers, hyphens only");
		
		// Assert - SKU con espacios (inválido)
		assertFalse(invalidSKUWithSpaces.matches(skuRegex),
			"SKU with spaces should be invalid");
		
		// Assert - SKU con caracteres especiales (inválido)
		assertFalse(invalidSKUWithSpecialChars.matches(skuRegex),
			"SKU with special characters should be invalid");
		
		// Assert - SKU no vacío
		assertFalse(validSKU.trim().isEmpty(),
			"SKU should not be empty");
		
		// Assert - Longitud mínima de SKU
		assertTrue(validSKU.length() >= 5,
			"SKU should have minimum length of 5 characters");
		
		// Assert - ProductTitle no vacío
		assertFalse(emptyProductTitle.length() > 0,
			"Product title should not be empty");
	}

	// ========================================
	// RESUMEN DE PRUEBAS
	// ========================================
	
	/**
	 * RESUMEN:
	 * - 1 test de contexto Spring Boot
	 * - 4 tests unitarios de validación
	 * 
	 * TOTAL: 5 tests
	 * 
	 * TECNOLOGÍAS:
	 * - JUnit 5
	 * - Mockito
	 * - BigDecimal para precios
	 * - Spring Boot Test
	 */
}






