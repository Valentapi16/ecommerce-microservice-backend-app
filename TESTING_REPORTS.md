# 📊 Guía de Reportes de Pruebas y Rendimiento

Esta guía te muestra cómo generar y visualizar reportes profesionales de las pruebas unitarias y de rendimiento.

---

## 📋 **Tabla de Contenido**

1. [Reportes JUnit (Pruebas Unitarias)](#1-reportes-junit-pruebas-unitarias)
2. [Reportes Locust (Pruebas de Rendimiento)](#2-reportes-locust-pruebas-de-rendimiento)
3. [Reportes Agregados](#3-reportes-agregados)

---

## 1️⃣ **Reportes JUnit (Pruebas Unitarias)**

### ✅ **Opción A: Reporte HTML Completo (RECOMENDADO)**

Genera un sitio web completo con todos los reportes:

```bash
# Ejecutar pruebas y generar reporte HTML
mvn clean test site

# O para un servicio específico:
cd user-service
mvn clean test site
```

**📂 Ubicación del reporte:**
```
target/site/surefire-report.html
```

**🌐 Abrir el reporte:**
```bash
# En Windows PowerShell
start target/site/surefire-report.html

# O manualmente:
# Navega a: target/site/surefire-report.html y ábrelo en tu navegador
```

---

### ✅ **Opción B: Reporte Rápido (Solo Surefire)**

Si solo quieres el reporte de pruebas sin todo el sitio:

```bash
# Ejecutar pruebas y generar solo reporte Surefire
mvn clean test
mvn surefire-report:report

# Abrir reporte
start target/site/surefire-report.html
```

---

### ✅ **Opción C: Reporte Agregado de TODOS los Servicios**

Para ver un reporte consolidado con TODAS las pruebas:

```bash
# Desde la raíz del proyecto
mvn clean test
mvn surefire-report:report-only
mvn site -DgenerateReports=false

# Abrir reporte agregado
start target/site/surefire-report.html
```

**📊 Este reporte mostrará:**
- ✅ Total de pruebas ejecutadas
- ✅ Pruebas exitosas
- ❌ Pruebas fallidas
- ⏱️ Tiempo de ejecución
- 📈 Tasa de éxito
- 📄 Detalle por servicio y clase de prueba

---

### 📸 **Ejemplo de Output en Terminal:**

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.selimhorri.app.UserServiceApplicationTests
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.5 sec

Results :

Tests run: 5, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
```

---

## 2️⃣ **Reportes Locust (Pruebas de Rendimiento)**

### 🚀 **Configuración Inicial**

#### **Paso 1: Instalar Locust**

```bash
# Navegar a la carpeta de pruebas de rendimiento
cd performance-tests

# Instalar dependencias
pip install -r requirements.txt

# Verificar instalación
locust --version
```

---

### 🎯 **Opción A: Interfaz Web Interactiva (RECOMENDADO para Demos)**

#### **Paso 1: Iniciar el Servidor de Servicios**

Primero, asegúrate de que los servicios estén corriendo:

```bash
# Opción 1: Con Docker Compose
docker-compose up -d

# Opción 2: Manualmente (cada servicio)
cd user-service
./mvnw spring-boot:run

cd ../product-service
./mvnw spring-boot:run

# ... etc.
```

#### **Paso 2: Iniciar Locust con UI Web**

```bash
cd performance-tests

# Iniciar Locust con interfaz web
locust -f locustfile.py --host=http://localhost:8080
```

#### **Paso 3: Abrir la Interfaz Web**

1. Abre tu navegador en: **http://localhost:8089**

2. Verás una pantalla de configuración:
   - **Number of users (peak concurrency)**: 100 (usuarios simultáneos)
   - **Spawn rate (users started/second)**: 10 (usuarios por segundo)
   - **Host**: `http://localhost:8080` (ya configurado)

3. Click en **"Start Swarming"** 🐝

#### **Paso 4: Visualizar Métricas en Tiempo Real**

La interfaz web de Locust muestra:

📊 **Pestaña "Statistics":**
- Requests/segundo (RPS)
- Tiempo de respuesta promedio
- Percentiles (50%, 66%, 75%, 80%, 90%, 95%, 98%, 99%)
- Tasa de fallos
- Tamaño de respuesta

📈 **Pestaña "Charts":**
- Gráficos en tiempo real de:
  - Total Requests per Second
  - Response Times (mediana)
  - Number of Users

📉 **Pestaña "Failures":**
- Errores detectados durante las pruebas

📥 **Pestaña "Download Data":**
- Descargar reportes en CSV
- Descargar estadísticas completas

---

### 📊 **Opción B: Modo Headless (Sin UI, Para CI/CD)**

Para ejecutar pruebas automáticas y generar reportes:

```bash
cd performance-tests

# Ejecutar con reporte HTML
locust -f locustfile.py \
  --host=http://localhost:8080 \
  --users=100 \
  --spawn-rate=10 \
  --run-time=2m \
  --headless \
  --html=reports/locust_report_$(date +%Y%m%d_%H%M%S).html \
  --csv=reports/locust_data

# En Windows PowerShell:
locust -f locustfile.py --host=http://localhost:8080 --users=100 --spawn-rate=10 --run-time=2m --headless --html=reports/locust_report.html --csv=reports/locust_data
```

**📂 Reportes generados:**
```
performance-tests/reports/
├── locust_report.html       (Reporte HTML visual)
├── locust_data_stats.csv    (Estadísticas detalladas)
├── locust_data_failures.csv (Fallos registrados)
└── locust_data_history.csv  (Historial de rendimiento)
```

**🌐 Abrir reporte:**
```bash
# Windows
start reports/locust_report.html

# Linux/Mac
open reports/locust_report.html
```

---

### 🎬 **Escenarios de Prueba Disponibles**

El `locustfile.py` incluye 4 escenarios:

1. **🛒 E-commerce User Journey** (Usuario completo)
   - Login → Ver productos → Agregar favoritos → Crear orden → Logout

2. **🔍 Browse Products** (Navegación)
   - Búsqueda y visualización de productos

3. **❤️ Manage Favourites** (Favoritos)
   - Agregar y gestionar productos favoritos

4. **📦 Order Flow** (Compras)
   - Crear y confirmar órdenes

---

### 📸 **Ejemplo de Métricas de Locust:**

```
Type     Name                              # reqs    # fails  |   Avg   Min   Max  Median  |   req/s failures/s
---------|----------------------------------|----------|---------|-------------------------|--------------------
GET      /api/products                      1250        0     |    45    12   234      38  |    41.7     0.00
POST     /api/orders                         480        2     |    89    34   456      78  |    16.0     0.07
GET      /api/users/profile                  950        0     |    32    10   178      28  |    31.7     0.00
---------|----------------------------------|----------|---------|-------------------------|--------------------
         Aggregated                          2680        2     |    52    10   456      35  |    89.3     0.07

Response time percentiles (approximated):
Type     Name                              50%   66%   75%   80%   90%   95%   98%   99%  99.9% 99.99%  100% # reqs
---------|----------------------------------|-----|-----|-----|-----|-----|-----|-----|-----|-----|------|--------|-------
GET      /api/products                       38    48    58    65    89   112   145   178   220    234    234   1250
POST     /api/orders                         78    95   123   145   201   267   345   398   445    456    456    480
```

---

## 3️⃣ **Reportes Agregados**

### 📊 **Reporte Final para Presentación**

Para generar un reporte completo profesional:

#### **Paso 1: Ejecutar todas las pruebas**

```bash
# Pruebas unitarias
mvn clean test
mvn surefire-report:report

# Pruebas de rendimiento (con interfaz web activa)
cd performance-tests
locust -f locustfile.py --host=http://localhost:8080
# Ejecutar desde la web UI y descargar reportes
```

#### **Paso 2: Ubicación de los reportes**

```
📁 ecommerce-microservice-backend-app/
├── 📄 target/site/surefire-report.html         (Pruebas unitarias agregadas)
├── 📁 user-service/target/site/surefire-report.html
├── 📁 product-service/target/site/surefire-report.html
├── 📁 order-service/target/site/surefire-report.html
└── 📁 performance-tests/
    └── 📄 reports/locust_report.html           (Pruebas de rendimiento)
```

---

## 🎯 **Comandos Rápidos para Demos**

### Para Mostrar Pruebas Unitarias:

```bash
# 1. Ejecutar pruebas y generar reporte
mvn clean test site

# 2. Abrir reporte
start target/site/surefire-report.html
```

### Para Mostrar Pruebas de Rendimiento:

```bash
# 1. Iniciar servicios
docker-compose up -d

# 2. Iniciar Locust con UI
cd performance-tests
locust -f locustfile.py --host=http://localhost:8080

# 3. Abrir navegador
start http://localhost:8089
```

---

## 📸 **Capturas de Pantalla Recomendadas para Reportes**

1. ✅ **Reporte Surefire HTML** mostrando:
   - Summary con "Tests: 31, Failures: 0, Errors: 0"
   - Lista de servicios testeados
   - Tiempo de ejecución

2. 📊 **Locust UI - Statistics Tab** mostrando:
   - Tabla de requests con métricas
   - RPS (requests per second)
   - Response times

3. 📈 **Locust UI - Charts Tab** mostrando:
   - Gráficos en tiempo real
   - Users activos
   - Response times

4. 📥 **Locust HTML Report** descargado mostrando:
   - Resumen de la prueba
   - Estadísticas detalladas
   - Distribución de tiempos de respuesta

---

## 🎓 **Para Tu Proyecto Académico**

### Estructura Recomendada de Presentación:

1. **Introducción a las Pruebas** (2 min)
   - Tipos de pruebas implementadas
   - Herramientas utilizadas (JUnit 5, Locust)

2. **Demo de Pruebas Unitarias** (3 min)
   - Mostrar ejecución en terminal
   - Abrir reporte HTML Surefire
   - Explicar métricas clave

3. **Demo de Pruebas de Rendimiento** (5 min)
   - Mostrar interfaz web de Locust
   - Ejecutar prueba con usuarios simulados
   - Analizar gráficos en tiempo real
   - Mostrar reporte HTML generado

4. **Conclusiones** (2 min)
   - Métricas obtenidas
   - Cobertura de pruebas
   - Mejoras identificadas

---

## 🚀 **Tips para la Demo**

✅ **Antes de la presentación:**
- Tener servicios corriendo en Docker
- Tener Locust instalado y probado
- Generar reportes previos como respaldo

✅ **Durante la demo:**
- Usar la interfaz web de Locust (más visual)
- Mostrar gráficos en tiempo real
- Tener reportes HTML abiertos en pestañas

✅ **Puntos clave a destacar:**
- 31 pruebas unitarias ejecutadas exitosamente
- Cobertura de servicios principales
- Métricas de rendimiento bajo carga
- Tiempos de respuesta aceptables

---

## 📞 **Solución de Problemas**

### Problema: Locust no encuentra el host

```bash
# Verificar que los servicios estén corriendo
docker ps

# Verificar conectividad
curl http://localhost:8080/actuator/health
```

### Problema: Reportes Maven no se generan

```bash
# Limpiar y regenerar
mvn clean
mvn test
mvn surefire-report:report
```

### Problema: Error al instalar Locust

```bash
# Actualizar pip
python -m pip install --upgrade pip

# Reinstalar locust
pip install locust --force-reinstall
```

---

## 📚 **Referencias**

- [Maven Surefire Report Plugin](https://maven.apache.org/surefire/maven-surefire-report-plugin/)
- [Locust Documentation](https://docs.locust.io/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

---

**✨ ¡Buena suerte con tu presentación! 🎓**
