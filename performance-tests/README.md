# 🚀 Performance Testing with Locust

Este directorio contiene las pruebas de rendimiento y carga para el sistema de e-commerce usando **Locust**.

## 📋 Contenido

- `locustfile.py` - 4 escenarios completos de pruebas de rendimiento
- `requirements.txt` - Dependencias de Python necesarias

## 🎯 Escenarios Implementados

### 1️⃣ Escenario de Registro de Usuarios
- **Clase:** `UserRegistrationScenario`
- **Endpoint:** `POST /api/users/register`
- **Objetivo:** Probar capacidad de registros simultáneos
- **Usuario:** `RegistrationUser` (weight=1)

### 2️⃣ Escenario de Navegación de Productos
- **Clase:** `ProductBrowsingScenario`
- **Endpoints:**
  - `GET /api/products` (peso 3)
  - `GET /api/products/{id}` (peso 2)
  - `GET /api/products/category/{id}` (peso 1)
- **Objetivo:** Prueba de lectura intensiva
- **Usuario:** `BrowsingUser` (weight=5 - mayoría de usuarios)

### 3️⃣ Escenario de Creación de Órdenes
- **Clase:** `OrderCreationScenario`
- **Flujo:** Productos → Orden → Pago
- **Objetivo:** Prueba de escritura intensiva
- **Usuario:** `ShoppingUser` (weight=2)

### 4️⃣ Escenario de Carga Mixta
- **Clase:** `MixedWorkloadScenario`
- **Operaciones:**
  - Browse products (peso 5)
  - View details (peso 3)
  - Create order (peso 2)
  - Add to favourites (peso 1)
  - Health check (peso 1)
- **Objetivo:** Simular carga realista con 100+ usuarios
- **Usuario:** `MixedUser` (weight=3)

## 🛠️ Instalación

### Requisitos
- Python 3.8+
- pip instalado

### Instalar dependencias
```powershell
cd performance-tests
pip install -r requirements.txt
```

## 🚦 Ejecución

### Modo 1: Interfaz Web (Recomendado)
```powershell
locust -f locustfile.py --host=http://localhost:8080
```

Luego abrir en el navegador: **http://localhost:8089**

**Configuración sugerida:**
- **Number of users (peak concurrency):** 100
- **Spawn rate (users started/second):** 10
- **Host:** http://localhost:8080

### Modo 2: Línea de Comandos (Headless)

#### Test Rápido (10 usuarios, 30 segundos)
```powershell
locust -f locustfile.py --host=http://localhost:8080 `
  --users 10 `
  --spawn-rate 2 `
  --run-time 30s `
  --headless
```

#### Test de Carga (100 usuarios, 2 minutos)
```powershell
locust -f locustfile.py --host=http://localhost:8080 `
  --users 100 `
  --spawn-rate 10 `
  --run-time 2m `
  --headless
```

#### Test de Estrés (500 usuarios, 5 minutos)
```powershell
locust -f locustfile.py --host=http://localhost:8080 `
  --users 500 `
  --spawn-rate 50 `
  --run-time 5m `
  --headless
```

### Modo 3: Escenario Específico

#### Solo Navegación de Productos
```powershell
locust -f locustfile.py --host=http://localhost:8080 `
  --users 50 `
  --spawn-rate 10 `
  BrowsingUser `
  --headless
```

#### Solo Creación de Órdenes
```powershell
locust -f locustfile.py --host=http://localhost:8080 `
  --users 20 `
  --spawn-rate 5 `
  ShoppingUser `
  --headless
```

## 📊 Interpretación de Resultados

### Métricas Principales

#### 1. Request/s (RPS)
- **Qué es:** Requests por segundo
- **Objetivo:** > 50 RPS para endpoints de lectura
- **Ejemplo:** 75 RPS = 75 peticiones procesadas por segundo

#### 2. Response Time (ms)
- **p50 (mediana):** 50% de las peticiones están por debajo de este tiempo
- **p90:** 90% de las peticiones están por debajo de este tiempo
- **p95:** 95% de las peticiones están por debajo de este tiempo
- **p99:** 99% de las peticiones están por debajo de este tiempo
- **max:** Tiempo máximo de respuesta

**Objetivos:**
- ✅ p50 < 500ms (excelente)
- ✅ p95 < 2000ms (aceptable)
- ⚠️ p99 < 5000ms (límite)

#### 3. Failures (%)
- **Qué es:** Porcentaje de peticiones fallidas
- **Objetivo:** < 5%
- **Causas comunes:**
  - Timeouts
  - Errores 5xx (servidor)
  - Stock insuficiente (esperado en algunos casos)

### Ejemplo de Reporte

```
Type     Name                          # reqs      # fails  |     Avg     Min     Max  Median  |   req/s failures/s
--------|----------------------------|-------|-------------|-------|-------|-------|-------|---------|-----------
GET      /api/products                  5432      12 (0.2%)  |     234      45    1234     210  |   90.53      0.20
GET      /api/products/[id]             3421       5 (0.1%)  |     156      23     987     140  |   57.02      0.08
POST     /api/orders                     876      15 (1.7%)  |     456      89    2345     420  |   14.60      0.25
--------|----------------------------|-------|-------------|-------|-------|-------|-------|---------|-----------
         Aggregated                     9729      32 (0.3%)  |     245      23    2345     198  |  162.15      0.53
```

**Interpretación:**
- ✅ GET /api/products: 90.53 RPS, 234ms avg, 0.2% failures → **EXCELENTE**
- ✅ GET /api/products/[id]: 57.02 RPS, 156ms avg, 0.1% failures → **EXCELENTE**
- ⚠️ POST /api/orders: 14.60 RPS, 456ms avg, 1.7% failures → **ACEPTABLE** (escritura más lenta)

## 🎯 Objetivos de Rendimiento

| Métrica | Objetivo | Crítico |
|---------|----------|---------|
| Response Time (p50) | < 500ms | < 1000ms |
| Response Time (p95) | < 2000ms | < 5000ms |
| Failures | < 5% | < 10% |
| RPS (lectura) | > 50 | > 20 |
| RPS (escritura) | > 10 | > 5 |
| Usuarios concurrentes | 100+ | 50+ |

## 📈 Gráficas y Reportes

### Durante la Ejecución (Web UI)
- **Total Requests per Second:** Gráfica en tiempo real de RPS
- **Response Times (ms):** Gráfica de percentiles (p50, p95)
- **Number of Users:** Cantidad de usuarios activos

### Después de la Ejecución
Locust genera un reporte HTML con:
- Estadísticas completas por endpoint
- Gráficas de rendimiento
- Distribución de tiempos de respuesta
- Tabla de failures

**Exportar reporte:**
```powershell
# El reporte se guarda automáticamente en locust_report.html
# O especificar ubicación:
locust -f locustfile.py --host=http://localhost:8080 `
  --users 100 `
  --spawn-rate 10 `
  --run-time 2m `
  --html=reports/load_test_report.html `
  --headless
```

## 🔧 Troubleshooting

### Error: "Connection refused"
**Causa:** API Gateway no está corriendo  
**Solución:**
```powershell
cd api-gateway
./mvnw spring-boot:run
```

### Error: "Max retries exceeded"
**Causa:** Servicio caído o sobrecargado  
**Solución:**
1. Verificar que todos los servicios estén corriendo
2. Reducir la carga (menos usuarios o menor spawn rate)
3. Verificar logs de los servicios

### Error: "ModuleNotFoundError: No module named 'locust'"
**Causa:** Locust no está instalado  
**Solución:**
```powershell
pip install locust
```

### Muchos Failures (>10%)
**Posibles causas:**
1. **Stock insuficiente:** Normal para POST /api/orders (esperado)
2. **Timeout:** Aumentar timeout en locustfile.py
3. **Base de datos lenta:** Optimizar queries o usar índices
4. **Recursos insuficientes:** Aumentar RAM/CPU de los servicios

## 🔍 Análisis Avanzado

### Identificar Bottlenecks

1. **Si Response Time es alto:**
   - Revisar logs de los microservicios
   - Verificar queries a la base de datos
   - Considerar caché (Redis)

2. **Si Failures es alto:**
   - Revisar conexiones a la base de datos
   - Verificar límites de conexiones (pool)
   - Revisar timeouts de Eureka/RestTemplate

3. **Si RPS es bajo:**
   - Aumentar workers de los servicios
   - Optimizar código (algoritmos, queries)
   - Escalar horizontalmente (más instancias)

## 📝 Notas Importantes

### Entorno de Pruebas
⚠️ **NUNCA ejecutar pruebas de carga en producción**

Usar entorno de staging/QA con:
- Base de datos separada
- Configuración similar a producción
- Datos de prueba (no reales)

### Datos de Prueba
Los tests usan datos generados automáticamente:
- Emails: `loadtest_TIMESTAMP_RANDOM@example.com`
- Usuarios: `LoadTestUser###`
- Órdenes: `Load Test Order TIMESTAMP`

### Limpieza Después de las Pruebas
```sql
-- Eliminar datos de prueba
DELETE FROM users WHERE email LIKE 'loadtest_%@example.com';
DELETE FROM orders WHERE order_desc LIKE 'Load Test Order%';
```

## 🎓 Recursos Adicionales

- **Documentación oficial de Locust:** https://docs.locust.io/
- **Guía de Best Practices:** https://docs.locust.io/en/stable/writing-a-locustfile.html
- **Ejemplos de Locustfiles:** https://github.com/locustio/locust/tree/master/examples

## ✅ Checklist de Ejecución

Antes de ejecutar las pruebas:

- [ ] Todos los microservicios están corriendo
- [ ] API Gateway está en puerto 8080
- [ ] Eureka está en puerto 8761
- [ ] Base de datos está accesible
- [ ] Locust está instalado (`pip install locust`)
- [ ] Entorno de pruebas (NO producción)
- [ ] Datos de prueba preparados

---

**¿Necesitas ayuda?** Revisa los logs de los servicios o consulta `TESTS_SUMMARY.md` en la raíz del proyecto.
