# 🚀 Guía Rápida de Ejecución de Pruebas

**Última actualización:** 29 de enero de 2025

## 📌 Inicio Rápido (5 minutos)

### Paso 1: Levantar todos los servicios
```powershell
# Opción A: Con Docker Compose (recomendado)
docker-compose up -d

# Opción B: Con Maven (desarrollo)
# En terminales separadas:
cd service-discovery; ./mvnw spring-boot:run  # Puerto 8761
cd cloud-config; ./mvnw spring-boot:run       # Puerto 9296
cd api-gateway; ./mvnw spring-boot:run        # Puerto 8080
cd user-service; ./mvnw spring-boot:run       # Puerto 8081
cd product-service; ./mvnw spring-boot:run    # Puerto 8082
cd order-service; ./mvnw spring-boot:run      # Puerto 8083
cd payment-service; ./mvnw spring-boot:run    # Puerto 8084
cd shipping-service; ./mvnw spring-boot:run   # Puerto 8085
cd favourite-service; ./mvnw spring-boot:run  # Puerto 8086
```

### Paso 2: Verificar que todos los servicios estén corriendo
```powershell
# Verificar Eureka Dashboard
Start-Process "http://localhost:8761"

# Verificar API Gateway
curl http://localhost:8080/actuator/health
```

### Paso 3: Ejecutar pruebas

#### Pruebas Unitarias (rápidas - 2 min)
```powershell
cd user-service; ./mvnw test
cd ../product-service; ./mvnw test
cd ../order-service; ./mvnw test
```

#### Pruebas de Integración (moderadas - 5 min)
```powershell
cd order-service; ./mvnw test -Dtest=OrderServiceIntegrationTest
cd ../shipping-service; ./mvnw test -Dtest=ShippingServiceIntegrationTest
cd ../favourite-service; ./mvnw test -Dtest=FavouriteServiceIntegrationTest
```

#### Pruebas E2E (lentas - 10 min)
```powershell
cd src/test/java/com/selimhorri/app/e2e
mvn test
```

#### Pruebas de Rendimiento (2-5 min)
```powershell
cd performance-tests
pip install locust
locust -f locustfile.py --host=http://localhost:8080
# Abrir http://localhost:8089
# Configurar: 100 users, 10 spawn rate, 2 minutes
```

---

## 📊 Resumen de Archivos de Pruebas

### Pruebas Unitarias
| Archivo | Ubicación | Tests |
|---------|-----------|-------|
| UserServiceUnitTest.java | user-service/src/test/java/.../service/ | 4 |
| ProductServiceUnitTest.java | product-service/src/test/java/.../service/ | 4 |
| OrderServiceUnitTest.java | order-service/src/test/java/.../service/ | 5 |

### Pruebas de Integración
| Archivo | Ubicación | Tests |
|---------|-----------|-------|
| OrderServiceIntegrationTest.java | order-service/src/test/java/.../integration/ | 3 |
| ShippingServiceIntegrationTest.java | shipping-service/src/test/java/.../integration/ | 3 |
| FavouriteServiceIntegrationTest.java | favourite-service/src/test/java/.../integration/ | 3 |

### Pruebas E2E
| Archivo | Ubicación | Tests |
|---------|-----------|-------|
| UserRegistrationLoginE2ETest.java | src/test/java/.../e2e/ | 4 |
| PurchaseFlowE2ETest.java | src/test/java/.../e2e/ | 6 |
| FavouritesFlowE2ETest.java | src/test/java/.../e2e/ | 8 |

### Pruebas de Rendimiento
| Archivo | Ubicación | Escenarios |
|---------|-----------|------------|
| locustfile.py | performance-tests/ | 4 |

---

## 🎯 Comandos Esenciales

### Ejecutar TODAS las pruebas de un servicio
```powershell
cd user-service
./mvnw clean test
```

### Ejecutar una prueba específica
```powershell
./mvnw test -Dtest=UserServiceUnitTest
./mvnw test -Dtest=UserServiceUnitTest#testEmailValidation
```

### Ver reporte de cobertura
```powershell
./mvnw jacoco:report
# Abrir: target/site/jacoco/index.html
```

### Ejecutar pruebas con logs detallados
```powershell
./mvnw test -X
```

---

## ✅ Checklist Pre-Ejecución

Antes de ejecutar las pruebas, verificar:

- [ ] Java 17+ instalado (`java -version`)
- [ ] Maven instalado (`mvn -version`)
- [ ] Python 3.8+ instalado (solo para Locust)
- [ ] Docker Desktop corriendo (si usas docker-compose)
- [ ] Puerto 8080 libre (API Gateway)
- [ ] Puerto 8761 libre (Eureka)
- [ ] Base de datos accesible
- [ ] Suficiente RAM disponible (4GB+ recomendado)

---

## 🐛 Troubleshooting Rápido

### ❌ Error: "Address already in use"
```powershell
# Ver qué proceso usa el puerto 8080
netstat -ano | findstr :8080
# Matar el proceso (reemplazar <PID> con el número que aparece)
taskkill /PID <PID> /F
```

### ❌ Error: "Unable to connect to Eureka"
```powershell
# Verificar que Eureka esté corriendo
curl http://localhost:8761/eureka/apps
# Si no responde, levantar service-discovery primero
cd service-discovery
./mvnw spring-boot:run
```

### ❌ Tests fallan con "Connection refused"
**Solución:** Asegurarse de que todos los servicios estén corriendo antes de ejecutar pruebas E2E.

### ❌ Locust: "ModuleNotFoundError"
```powershell
# Instalar dependencias
cd performance-tests
pip install -r requirements.txt
```

---

## 📈 Resultados Esperados

### Pruebas Unitarias
```
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
✅ SUCCESS
```

### Pruebas de Integración
```
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
✅ SUCCESS
```

### Pruebas E2E
```
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
✅ SUCCESS
```

### Pruebas de Rendimiento (Locust)
```
Type        Name                     # reqs    # fails  Avg    Min    Max   Median  req/s
GET         /api/products            5000      25(0.5%) 234ms  45ms   1.2s  210ms   83.3
POST        /api/orders              1200      18(1.5%) 456ms  89ms   2.3s  420ms   20.0
✅ Response Time p95 < 2000ms
✅ Failures < 5%
✅ Sistema estable con 100 usuarios
```

---

## 📚 Documentación Completa

Para más detalles, consultar:
- **TESTS_SUMMARY.md** - Resumen completo de todas las pruebas
- **performance-tests/README.md** - Guía detallada de Locust
- **JENKINS_PIPELINE_DOCS.md** - Documentación del pipeline CI/CD

---

## 💡 Tips

1. **Ejecutar pruebas en orden:** Unitarias → Integración → E2E → Rendimiento
2. **Usar perfil de test:** Agregar `-Dspring.profiles.active=test` para usar configuración de test
3. **Generar reportes:** Agregar `-Dmaven.test.failure.ignore=true` para generar reporte aunque haya fallos
4. **Paralelizar tests:** Agregar `-DforkCount=4` para ejecutar tests en paralelo

---

**¿Dudas?** Revisar los logs de los servicios o consultar la documentación completa en `TESTS_SUMMARY.md`
