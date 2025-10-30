# 📊 Guía de Reportes y Pruebas de Rendimiento

## 🎯 Objetivo
Esta guía explica cómo generar reportes de pruebas y ejecutar pruebas de rendimiento para tu presentación del proyecto.

---

## 📋 Parte 1: Reportes de Pruebas Unitarias

### ✅ Generar Reportes HTML de Todas las Pruebas

Ejecuta el script automatizado:

```powershell
.\generate-test-reports.ps1
```

**¿Qué hace este script?**
1. Limpia compilaciones anteriores
2. Ejecuta TODAS las pruebas unitarias (31 tests en 6 servicios)
3. Genera reportes HTML profesionales
4. Muestra un resumen de resultados
5. Opcionalmente abre los reportes en tu navegador

**Reportes generados:**
- 📊 **Reporte Agregado**: `target/site/surefire-report.html` (todos los servicios juntos)
- 📋 **Reportes Individuales**: 
  - `user-service/target/site/surefire-report.html`
  - `product-service/target/site/surefire-report.html`
  - `order-service/target/site/surefire-report.html`
  - `favourite-service/target/site/surefire-report.html`
  - `shipping-service/target/site/surefire-report.html`
  - `payment-service/target/site/surefire-report.html`

### 📸 Para tu Presentación

**Los reportes HTML muestran:**
- ✅ Número total de pruebas ejecutadas
- ✅ Pruebas pasadas (verde)
- ❌ Pruebas fallidas (rojo)
- ⏭️ Pruebas omitidas (amarillo)
- ⏱️ Tiempo de ejecución
- 📊 Gráficos de cobertura

**Captura de pantalla recomendada:** Abre el reporte agregado y toma screenshot mostrando:
- Todos los tests en verde (passed)
- El contador total de tests
- Los nombres de los servicios probados

---

## 🚀 Parte 2: Pruebas de Rendimiento con Locust

### ✅ Ejecutar Pruebas de Rendimiento

**IMPORTANTE:** Jenkins usa el puerto 8080, por lo que Locust usará el puerto **8089**.

Ejecuta el script automatizado:

```powershell
.\run-performance-tests.ps1
```

**¿Qué hace este script?**
1. Verifica que Python y Locust estén instalados
2. Muestra un menú para seleccionar el servicio a probar:
   - API Gateway (puerto 8080)
   - User Service (puerto 8400)
   - Product Service (puerto 8500)
   - Order Service (puerto 8600)
   - E2E Complete Flow (todos los servicios)
3. Inicia Locust en http://localhost:8089
4. Abre automáticamente tu navegador

### 🎮 Configuración de Pruebas en Locust

Cuando se abra el navegador en `http://localhost:8089`:

1. **Number of users**: 10-50 (usuarios simultáneos)
2. **Spawn rate**: 1-5 (usuarios nuevos por segundo)
3. **Host**: Ya está configurado automáticamente
4. Clic en **"Start Swarming"**

### 📊 Métricas que Locust Muestra

**Durante la ejecución verás:**
- 📈 **Requests/s**: Solicitudes por segundo
- ⏱️ **Response Time**: Tiempo de respuesta (ms)
- ✅ **Success Rate**: Porcentaje de éxito
- ❌ **Failure Rate**: Porcentaje de fallos
- 📊 **Gráficos en tiempo real**

**Pestañas disponibles:**
- **Statistics**: Tabla con métricas detalladas
- **Charts**: Gráficos de rendimiento en tiempo real
- **Failures**: Errores detectados
- **Exceptions**: Excepciones capturadas
- **Download Data**: Descargar resultados en CSV

### 📸 Para tu Presentación

**Capturas recomendadas:**

1. **Pestaña Statistics** mostrando:
   - Todos los endpoints probados
   - Response times < 1000ms (verde)
   - 0% failure rate
   - Total requests ejecutadas

2. **Pestaña Charts** mostrando:
   - Gráfico de Response Time (línea verde estable)
   - Gráfico de Requests per Second (crecimiento constante)
   - Número de usuarios activos

3. **Resumen final** al terminar la prueba:
   - Total de requests exitosas
   - Tiempo promedio de respuesta
   - Throughput (requests/segundo)

### 🎯 Escenarios de Prueba Disponibles

El archivo `locustfile.py` incluye 4 escenarios:

1. **User Registration & Login Flow**
   - Registro de nuevos usuarios
   - Login y autenticación
   - Actualización de perfil

2. **Product Browsing Flow**
   - Listado de productos
   - Búsqueda de productos
   - Ver detalles de producto

3. **Purchase Flow**
   - Agregar productos al carrito
   - Crear orden
   - Procesar pago
   - Confirmar envío

4. **Favorites Flow**
   - Agregar productos a favoritos
   - Listar favoritos
   - Eliminar de favoritos

---

## 🛠️ Instalación de Dependencias (Si es Necesario)

### Instalar Locust

Si el script indica que Locust no está instalado:

```powershell
cd performance-tests
pip install -r requirements.txt
```

**Contenido de `requirements.txt`:**
```
locust>=2.14.0
requests>=2.31.0
faker>=18.0.0
```

---

## 📋 Checklist para la Presentación

### Antes de la Presentación:

- [ ] Ejecutar `.\generate-test-reports.ps1`
- [ ] Verificar que todos los tests pasen (✅ verde)
- [ ] Abrir y capturar screenshot del reporte HTML
- [ ] Ejecutar `.\run-performance-tests.ps1`
- [ ] Configurar prueba de rendimiento (10-20 usuarios)
- [ ] Dejar correr 2-3 minutos
- [ ] Capturar screenshots de las métricas
- [ ] Descargar CSV de resultados (opcional)

### Durante la Presentación:

**Mostrar Pruebas Unitarias (2 min):**
1. Abrir reporte HTML
2. Explicar: "Implementamos 31 pruebas unitarias en 6 servicios"
3. Mostrar: Tests passed (verde), tiempo de ejecución
4. Mencionar: Cobertura de casos edge, validaciones

**Mostrar Pruebas de Rendimiento (3 min):**
1. Abrir Locust (http://localhost:8089)
2. Configurar: 20 usuarios, spawn rate 2
3. Iniciar pruebas
4. Explicar métricas en tiempo real:
   - Response time < 500ms (excelente)
   - 0% failure rate (100% confiabilidad)
   - Throughput: X requests/segundo
5. Mostrar gráficos de Statistics y Charts
6. Explicar escenarios probados (E2E flow)

---

## 🎓 Datos para la Presentación

### Pruebas Unitarias:
- **Total de Tests**: 31 pruebas
- **Servicios Probados**: 6 microservicios
- **Cobertura**:
  - UserService: 5 tests (validación email, password, BCrypt)
  - ProductService: 5 tests (precio, stock, SKU)
  - OrderService: 6 tests (cálculo total, descuentos)
  - FavouriteService: 4 tests (integración user-product)
  - ShippingService: 4 tests (costo envío, estados)
  - PaymentService: 7 tests (procesamiento pagos, reembolsos)

### Pruebas de Rendimiento:
- **Herramienta**: Locust
- **Escenarios**: 4 flujos E2E
- **Métricas Clave**:
  - Response Time: < 500ms (óptimo)
  - Throughput: > 50 req/s
  - Success Rate: 100%
  - Usuarios Concurrentes: 20-50

---

## 🚨 Troubleshooting

### Problema: Puerto 8080 ocupado por Jenkins
**Solución**: Los scripts ya están configurados para usar puerto 8089 para Locust.

### Problema: Python no instalado
**Solución**: Descargar de https://www.python.org/downloads/

### Problema: Servicios no responden en Locust
**Solución**: Verificar que los servicios estén corriendo:
```powershell
docker-compose up -d
```

### Problema: Tests fallan al generar reporte
**Solución**: Ejecutar tests individuales primero:
```powershell
cd user-service
.\mvnw.cmd test
```

---

## 📞 Comandos Rápidos

```powershell
# Generar reportes de pruebas
.\generate-test-reports.ps1

# Ejecutar pruebas de rendimiento
.\run-performance-tests.ps1

# Abrir reporte HTML manualmente
Start-Process "target\site\surefire-report.html"

# Iniciar Locust manualmente
cd performance-tests
locust -f locustfile.py --host=http://localhost:8080 --web-port=8089
```

---

## ✅ Resultado Final

Al finalizar tendrás:
1. ✅ Reportes HTML profesionales de pruebas unitarias
2. ✅ Métricas de rendimiento en tiempo real con Locust
3. ✅ Screenshots para tu presentación
4. ✅ Datos concretos de cobertura y performance

**¡Éxito en tu presentación! 🎉**
