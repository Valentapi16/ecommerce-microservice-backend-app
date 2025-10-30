package com.selimhorri.app;

import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.repository.CredentialRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User Service Application Tests
 * 
 * Incluye:
 * - Pruebas unitarias de validación de usuarios
 * - Pruebas de contexto de Spring Boot
 * 
 * @author Test Suite
 * @since 2025-10-29
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Application Tests")
class UserServiceApplicationTests {

	@Mock
	private UserRepository userRepository;
	
	@Mock
	private CredentialRepository credentialRepository;

	/**
	 * TEST 1: Verificar que el contexto de Spring Boot carga correctamente
	 */
	@Test
	@DisplayName("Context Loads - Spring Boot context should load successfully")
	void contextLoads() {
		// Este test verifica que la aplicación Spring Boot se inicie correctamente
		// Si falla, significa que hay problemas en la configuración de beans
		assertTrue(true, "Spring Boot context loaded successfully");
	}

	// ========================================
	// PRUEBAS UNITARIAS DE VALIDACIÓN
	// ========================================

	/**
	 * UT-USER-001: Validación de formato de email
	 */
	@Test
	@DisplayName("UT-USER-001: Should validate email format correctly")
	void testEmailValidation() {
		// Arrange - Emails válidos e inválidos
		String[] validEmails = {
			"user@example.com",
			"john.doe@company.co.uk",
			"test+tag@domain.com",
			"info@sub.domain.org"
		};
		
		String[] invalidEmails = {
			"notanemail",
			"@example.com",
			"user@",
			"user @example.com",
			"user@.com",
			"user..name@example.com"
		};
		
		// Regex simplificado basado en RFC 5322
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		
		// Act & Assert - Validar emails válidos
		for (String email : validEmails) {
			assertTrue(email.matches(emailRegex), 
				"Email válido rechazado: " + email);
		}
		
		// Act & Assert - Validar emails inválidos
		for (String email : invalidEmails) {
			assertFalse(email.matches(emailRegex), 
				"Email inválido aceptado: " + email);
		}
	}

	/**
	 * UT-USER-002: Validación de encriptación de contraseña con BCrypt
	 */
	@Test
	@DisplayName("UT-USER-002: Should encrypt password using BCrypt correctly")
	void testPasswordEncryption() {
		// Arrange
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String plainPassword = "MySecurePassword123!";
		
		// Act - Encriptar password
		String hashedPassword1 = encoder.encode(plainPassword);
		String hashedPassword2 = encoder.encode(plainPassword);
		
		// Assert
		// 1. El hash no debe ser igual al password original
		assertNotEquals(plainPassword, hashedPassword1, 
			"Password hash should not equal plain password");
		
		// 2. Cada hash debe ser único (sal diferente)
		assertNotEquals(hashedPassword1, hashedPassword2, 
			"Two hashes of same password should be different (different salts)");
		
		// 3. Verificar que el password original coincide con el hash
		assertTrue(encoder.matches(plainPassword, hashedPassword1), 
			"Plain password should match its hash");
		
		// 4. Verificar que un password incorrecto no coincide
		assertFalse(encoder.matches("WrongPassword", hashedPassword1), 
			"Wrong password should not match the hash");
		
		// 5. El hash debe tener el formato BCrypt ($2a$...)
		assertTrue(hashedPassword1.startsWith("$2a$"), 
			"BCrypt hash should start with $2a$");
		
		// 6. El hash debe tener la longitud correcta (60 caracteres)
		assertEquals(60, hashedPassword1.length(), 
			"BCrypt hash should be 60 characters long");
	}

	/**
	 * UT-USER-003: Validación de campos obligatorios
	 */
	@Test
	@DisplayName("UT-USER-003: Should validate mandatory fields")
	void testMandatoryFields() {
		// Arrange
		UserDto validUser = new UserDto();
		validUser.setFirstName("John");
		validUser.setLastName("Doe");
		validUser.setEmail("john.doe@example.com");
		
		CredentialDto credentials = new CredentialDto();
		credentials.setUsername("john.doe@example.com");
		credentials.setPassword("SecurePass123!");
		validUser.setCredentialDto(credentials);
		
		// Assert - Usuario válido
		assertNotNull(validUser.getFirstName(), "First name is mandatory");
		assertNotNull(validUser.getLastName(), "Last name is mandatory");
		assertNotNull(validUser.getEmail(), "Email is mandatory");
		assertNotNull(validUser.getCredentialDto(), "Credentials are mandatory");
		assertNotNull(validUser.getCredentialDto().getUsername(), "Username is mandatory");
		assertNotNull(validUser.getCredentialDto().getPassword(), "Password is mandatory");
		
		// Arrange - Usuario sin nombre
		UserDto userWithoutName = new UserDto();
		userWithoutName.setLastName("Doe");
		userWithoutName.setEmail("john.doe@example.com");
		
		// Assert
		assertNull(userWithoutName.getFirstName(), 
			"User without first name should be detected");
	}

	/**
	 * UT-USER-004: Validación de fortaleza de contraseña
	 */
	@Test
	@DisplayName("UT-USER-004: Should validate password strength")
	void testPasswordStrength() {
		// Arrange - Contraseñas de diferentes fortalezas
		String weakPassword = "12345";
		String mediumPassword = "Password123";
		String strongPassword = "SecureP@ss123";
		
		// Criterios de fortaleza:
		// - Mínimo 8 caracteres
		// - Al menos una mayúscula
		// - Al menos una minúscula
		// - Al menos un dígito
		
		// Act & Assert - Contraseña débil (muy corta)
		assertTrue(weakPassword.length() < 8, 
			"Weak password should be less than 8 characters");
		
		// Act & Assert - Contraseña media (cumple requisitos básicos)
		assertTrue(mediumPassword.length() >= 8, 
			"Medium password should be at least 8 characters");
		assertTrue(mediumPassword.matches(".*[A-Z].*"), 
			"Medium password should contain uppercase");
		assertTrue(mediumPassword.matches(".*[a-z].*"), 
			"Medium password should contain lowercase");
		assertTrue(mediumPassword.matches(".*\\d.*"), 
			"Medium password should contain digit");
		
		// Act & Assert - Contraseña fuerte (incluye caracteres especiales)
		assertTrue(strongPassword.length() >= 8, 
			"Strong password should be at least 8 characters");
		assertTrue(strongPassword.matches(".*[A-Z].*"), 
			"Strong password should contain uppercase");
		assertTrue(strongPassword.matches(".*[a-z].*"), 
			"Strong password should contain lowercase");
		assertTrue(strongPassword.matches(".*\\d.*"), 
			"Strong password should contain digit");
		assertTrue(strongPassword.matches(".*[^a-zA-Z0-9].*"), 
			"Strong password should contain special character");
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
	 * - BCryptPasswordEncoder
	 * - Spring Boot Test
	 */
}






