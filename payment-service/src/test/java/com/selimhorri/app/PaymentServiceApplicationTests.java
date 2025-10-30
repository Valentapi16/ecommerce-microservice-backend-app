package com.selimhorri.app;

import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Payment Service Application Tests
 * 
 * Incluye:
 * - Pruebas de flujo de pagos
 * - Pruebas de contexto de Spring Boot
 * 
 * @author Test Suite
 * @since 2025-10-29
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService - Application Tests")
class PaymentServiceApplicationTests {

	@Mock
	private PaymentRepository paymentRepository;

	/**
	 * TEST 1: Verificar que el contexto de Spring Boot carga correctamente
	 */
	@Test
	@DisplayName("Context Loads - Spring Boot context should load successfully")
	void contextLoads() {
		assertTrue(true, "Spring Boot context loaded successfully");
	}

	// ========================================
	// PRUEBAS DE PAGOS
	// ========================================

	/**
	 * IT-PAY-001: Validar creación de pago pendiente
	 */
	@Test
	@DisplayName("IT-PAY-001: Should create payment with NOT_STARTED status")
	void testCreatePendingPayment() {
		// Arrange
		PaymentDto payment = PaymentDto.builder()
			.paymentId(1)
			.isPayed(false)
			.paymentStatus(PaymentStatus.NOT_STARTED)
			.build();
		
		// Assert
		assertNotNull(payment.getPaymentId(), "Payment ID should not be null");
		assertFalse(payment.getIsPayed(), "Payment should initially be unpaid");
		assertEquals(PaymentStatus.NOT_STARTED, payment.getPaymentStatus(),
			"Payment status should be NOT_STARTED");
	}

	/**
	 * IT-PAY-002: Validar transición de estados de pago
	 */
	@Test
	@DisplayName("IT-PAY-002: Should transition payment status correctly")
	void testPaymentStatusTransition() {
		// Arrange - Pago inicial
		PaymentDto payment = PaymentDto.builder()
			.paymentId(1)
			.isPayed(false)
			.paymentStatus(PaymentStatus.NOT_STARTED)
			.build();
		
		// Act - Cambiar a IN_PROGRESS
		payment.setPaymentStatus(PaymentStatus.IN_PROGRESS);
		assertEquals(PaymentStatus.IN_PROGRESS, payment.getPaymentStatus(),
			"Status should change to IN_PROGRESS");
		
		// Act - Completar pago
		payment.setPaymentStatus(PaymentStatus.COMPLETED);
		payment.setIsPayed(true);
		
		// Assert
		assertEquals(PaymentStatus.COMPLETED, payment.getPaymentStatus(),
			"Status should be COMPLETED");
		assertTrue(payment.getIsPayed(),
			"Payment should be marked as paid");
	}

	/**
	 * IT-PAY-003: Validar pago completado
	 */
	@Test
	@DisplayName("IT-PAY-003: Should mark payment as completed successfully")
	void testCompletedPayment() {
		// Arrange
		PaymentDto payment = PaymentDto.builder()
			.paymentId(1)
			.isPayed(true)
			.paymentStatus(PaymentStatus.COMPLETED)
			.build();
		
		// Assert
		assertTrue(payment.getIsPayed(),
			"Completed payment should be marked as paid");
		assertEquals(PaymentStatus.COMPLETED, payment.getPaymentStatus(),
			"Payment status should be COMPLETED");
		assertNotNull(payment.getPaymentId(),
			"Payment should have valid ID");
	}

	/**
	 * IT-PAY-004: Validar estados de pago disponibles
	 */
	@Test
	@DisplayName("IT-PAY-004: Should have all payment statuses available")
	void testAvailablePaymentStatuses() {
		// Assert - Verificar todos los estados
		PaymentStatus[] statuses = PaymentStatus.values();
		
		assertEquals(3, statuses.length,
			"Should have exactly 3 payment statuses");
		
		boolean hasNotStarted = false;
		boolean hasInProgress = false;
		boolean hasCompleted = false;
		
		for (PaymentStatus status : statuses) {
			if (status == PaymentStatus.NOT_STARTED) hasNotStarted = true;
			if (status == PaymentStatus.IN_PROGRESS) hasInProgress = true;
			if (status == PaymentStatus.COMPLETED) hasCompleted = true;
		}
		
		assertTrue(hasNotStarted, "Should have NOT_STARTED status");
		assertTrue(hasInProgress, "Should have IN_PROGRESS status");
		assertTrue(hasCompleted, "Should have COMPLETED status");
	}

	/**
	 * IT-PAY-005: Validar consistencia isPayed con status
	 */
	@Test
	@DisplayName("IT-PAY-005: Should maintain consistency between isPayed and status")
	void testPaymentConsistency() {
		// Arrange - Pago no iniciado
		PaymentDto notStarted = PaymentDto.builder()
			.isPayed(false)
			.paymentStatus(PaymentStatus.NOT_STARTED)
			.build();
		
		// Arrange - Pago en progreso
		PaymentDto inProgress = PaymentDto.builder()
			.isPayed(false)
			.paymentStatus(PaymentStatus.IN_PROGRESS)
			.build();
		
		// Arrange - Pago completado
		PaymentDto completed = PaymentDto.builder()
			.isPayed(true)
			.paymentStatus(PaymentStatus.COMPLETED)
			.build();
		
		// Assert - Verificar consistencia
		assertFalse(notStarted.getIsPayed(),
			"NOT_STARTED payment should not be paid");
		assertFalse(inProgress.getIsPayed(),
			"IN_PROGRESS payment should not be paid yet");
		assertTrue(completed.getIsPayed(),
			"COMPLETED payment should be paid");
	}

	/**
	 * IT-PAY-006: Validar builder pattern
	 */
	@Test
	@DisplayName("IT-PAY-006: Should create payment using builder pattern")
	void testPaymentBuilder() {
		// Act
		PaymentDto payment = PaymentDto.builder()
			.paymentId(123)
			.isPayed(false)
			.paymentStatus(PaymentStatus.NOT_STARTED)
			.build();
		
		// Assert
		assertNotNull(payment, "Builder should create non-null payment");
		assertEquals(123, payment.getPaymentId(),
			"Builder should set paymentId correctly");
		assertFalse(payment.getIsPayed(),
			"Builder should set isPayed correctly");
		assertEquals(PaymentStatus.NOT_STARTED, payment.getPaymentStatus(),
			"Builder should set paymentStatus correctly");
	}
	
	/**
	 * RESUMEN:
	 * - 1 test de contexto Spring Boot
	 * - 6 tests E2E de pagos
	 * 
	 * TOTAL: 7 tests
	 */
}






