# Documentación del Pipeline de Jenkins - Punto 2

## 📋 Resumen Ejecutivo

Este pipeline de Jenkins cumple con el **Punto 2** del ejercicio: construcción automatizada de microservicios en entorno DEV con comunicación entre servicios.

---

## 🎯 Servicios Seleccionados

### Servicios de Infraestructura (3)
Estos servicios son **esenciales** para que los microservicios funcionales se comuniquen entre sí:

1. **service-discovery** (Eureka Server)
   - Registro y descubrimiento de servicios
   - Permite que los servicios se encuentren dinámicamente
   - Puerto: 8761

2. **cloud-config** (Spring Cloud Config Server)
   - Configuración centralizada
   - Gestión de propiedades por entorno
   - Puerto: 9296

3. **api-gateway** (Spring Cloud Gateway)
   - Punto de entrada único al sistema
   - Enrutamiento y balanceo de carga
   - Puerto: 8080

### Microservicios Funcionales (6)
Estos son los microservicios de negocio que implementan la funcionalidad del e-commerce:

1. **user-service**
   - Gestión de usuarios y credenciales
   - Puerto: 8081
   - **Proveedor**: otros servicios lo consumen

2. **product-service**
   - Gestión de productos y categorías
   - Puerto: 8082
   - **Proveedor**: otros servicios lo consumen

3. **payment-service**
   - Gestión de pagos de órdenes
   - Puerto: 8084
   - **Proveedor**: consumido por order-service

4. **order-service**
   - Gestión de órdenes y carritos de compra
   - Puerto: 8083
   - **Consumidor**: llama a user-service, product-service, payment-service

5. **shipping-service**
   - Gestión de envíos de órdenes
   - Puerto: 8085
   - **Consumidor**: llama a product-service, order-service

6. **favourite-service**
   - Gestión de productos favoritos de usuarios
   - Puerto: 8086
   - **Consumidor**: llama a user-service, product-service

**Total: 9 servicios (3 infraestructura + 6 funcionales)**

---

## 🔗 Comunicación Entre Servicios

### Flujo de Comunicación

```
┌─────────────────────────────────────────────────────────────────┐
│                        api-gateway (8080)                        │
│                    (Punto de entrada único)                      │
└────────────────────────────┬────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ user-service │    │product-service│   │payment-service│
│   (BASE)     │    │   (BASE)     │    │   (BASE)     │
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       │                   │                    │
       │ ┌─────────────────┘                    │
       │ │ ┌────────────────────────────────────┘
       │ │ │
       ▼ ▼ ▼
┌──────────────────────────────────────┐
│        order-service (8083)          │
│  Consume: user, product, payment     │
└──────────────────┬───────────────────┘
                   │
       ┌───────────┴───────────┐
       │                       │
       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐
│shipping-service │    │ favourite-service│
│  (8085)         │    │    (8086)        │
│Consume: product,│    │ Consume: user,   │
│  order          │    │   product        │
└─────────────────┘    └──────────────────┘
```

### Matriz de Dependencias

| Servicio            | Llama a                                    | Es llamado por                    |
|---------------------|-------------------------------------------|-----------------------------------|
| user-service        | -                                         | order, favourite                  |
| product-service     | -                                         | order, shipping, favourite        |
| payment-service     | -                                         | order                            |
| order-service       | user, product, payment                    | shipping                         |
| shipping-service    | product, order                            | -                                |
| favourite-service   | user, product                             | -                                |

### Evidencia de Comunicación (Código)

#### order-service → otros servicios
```java
// order-service/src/main/java/com/selimhorri/app/constant/AppConstant.java
public static final String USER_SERVICE_API_URL = "http://USER-SERVICE/user-service/api/users";
public static final String PRODUCT_SERVICE_API_URL = "http://PRODUCT-SERVICE/product-service/api/products";
public static final String PAYMENT_SERVICE_API_URL = "http://PAYMENT-SERVICE/payment-service/api/payments";

// order-service/src/main/java/com/selimhorri/app/service/impl/CartServiceImpl.java
c.setUserDto(this.restTemplate.getForObject(
    AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/" + c.getUserId(), 
    UserDto.class
));
```

#### shipping-service → product-service, order-service
```java
// shipping-service/src/main/java/com/selimhorri/app/service/impl/OrderItemServiceImpl.java
o.setProductDto(this.restTemplate.getForObject(
    AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/" + o.getProductDto().getProductId(), 
    ProductDto.class
));
o.setOrderDto(this.restTemplate.getForObject(
    AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/" + o.getOrderDto().getOrderId(), 
    OrderDto.class
));
```

#### favourite-service → user-service, product-service
```java
// favourite-service/src/main/java/com/selimhorri/app/service/impl/FavouriteServiceImpl.java
f.setUserDto(this.restTemplate.getForObject(
    AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/" + f.getUserId(), 
    UserDto.class
));
f.setProductDto(this.restTemplate.getForObject(
    AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/" + f.getProductId(), 
    ProductDto.class
));
```

---

## 🚀 Estructura del Pipeline

### Etapas del Pipeline

```
1. Checkout
   └─> Obtener código fuente del repositorio

2. Build All Services with Maven
   └─> Compilar TODOS los servicios (infraestructura + funcionales)
   └─> Extraer JARs de cada servicio

3. Run Tests
   └─> Ejecutar tests unitarios de todos los servicios
   └─> Continuar incluso si algunos tests fallan (DEV environment)

4. Docker Login
   └─> Autenticación en Docker Hub

5. Build & Push Infrastructure Images
   └─> Construir imágenes Docker de infraestructura PRIMERO
   └─> Subir a Docker Hub con tags: dev-${BUILD_NUMBER} y dev-latest

6. Build & Push Business Services Images
   └─> Construir imágenes Docker de microservicios funcionales
   └─> Subir a Docker Hub con tags: dev-${BUILD_NUMBER} y dev-latest

7. Post-Build Actions
   └─> Logout de Docker Hub
   └─> Reportar éxito/fallo con detalles
```

### Orden de Construcción

**¿Por qué construir en este orden?**

1. **Infraestructura primero**: Los servicios de descubrimiento (Eureka) y configuración (Config Server) deben estar disponibles antes que los microservicios funcionales
2. **Microservicios después**: Una vez la infraestructura está lista, los microservicios pueden registrarse y obtener su configuración

---

## 📦 Artefactos Generados

### Docker Images

Para cada servicio se generan **2 tags**:

1. **Versionado**: `valentapi16/${SERVICE}:dev-${BUILD_NUMBER}`
   - Ejemplo: `valentapi16/order-service:dev-42`
   - Útil para rollback y trazabilidad

2. **Latest DEV**: `valentapi16/${SERVICE}:dev-latest`
   - Ejemplo: `valentapi16/order-service:dev-latest`
   - Siempre apunta a la última build exitosa de DEV

**Total de imágenes por build**: 18 imágenes (9 servicios × 2 tags)

---

## 🧪 Testing

### Estrategia de Tests en DEV

- **Unit Tests**: Se ejecutan para todos los servicios
- **Tolerancia a fallos**: En DEV, los tests que fallan no detienen el build
  - Permite iterar rápidamente
  - Los errores se reportan pero no bloquean

### Comando de Tests
```bash
mvn test -pl service-discovery,cloud-config,api-gateway,product-service,user-service,payment-service,order-service,shipping-service,favourite-service
```

---

## 🔧 Variables de Entorno

```groovy
DOCKER_HUB_CREDENTIALS = credentials('dockerhub')  // Credenciales de Jenkins
DOCKER_HUB_REPO = "valentapi16"                    // Usuario de Docker Hub
INFRASTRUCTURE_SERVICES = "service-discovery cloud-config api-gateway"
BUSINESS_SERVICES = "product-service user-service payment-service order-service shipping-service favourite-service"
ENVIRONMENT = "dev"                                // Entorno de desarrollo
VERSION = "dev-${BUILD_NUMBER}"                    // Versionado automático
```

---

## ✅ Cumplimiento del Punto 2

### Requisitos del Ejercicio

| Requisito | Estado | Evidencia |
|-----------|--------|-----------|
| Configurar Jenkins | ✅ | Pipeline declarativo listo para Jenkins |
| Configurar Docker | ✅ | Build y push de imágenes Docker |
| Al menos 6 microservicios | ✅ | 6 microservicios funcionales seleccionados |
| Microservicios se comunican | ✅ | Comunicación mediante RestTemplate + Eureka |
| Pipeline de construcción | ✅ | Build completo con Maven + Docker |
| Entorno DEV | ✅ | Tags y variables para DEV |

### Servicios Adicionales (Infraestructura)

Además de los 6 microservicios requeridos, se incluyeron 3 servicios de infraestructura **necesarios** para que los microservicios funcionen correctamente:

- **service-discovery**: Sin Eureka, los servicios no pueden descubrirse (`http://USER-SERVICE/...` no se resolvería)
- **cloud-config**: Configuración centralizada por entorno
- **api-gateway**: Punto de entrada único (best practice)

**Total: 9 servicios** (excede el requisito mínimo de 6)

---

## 🔄 Flujo de Despliegue Típico

1. **Developer push** → GitHub (rama develop)
2. **Jenkins detecta cambio** → Inicia pipeline automáticamente
3. **Build**: Compilar con Maven
4. **Test**: Ejecutar tests unitarios
5. **Docker Build**: Crear imágenes
6. **Docker Push**: Subir a Docker Hub
7. **Listo para desplegar** → Las imágenes están disponibles para Kubernetes

---

## 📝 Notas Importantes

### ¿Por qué usar Docker multi-stage build?

En el pipeline, se usa un contenedor temporal para compilar:
- **Ventaja**: Entorno de compilación aislado y reproducible
- **Ventaja**: No requiere Maven instalado en Jenkins
- **Ventaja**: Misma versión de Java/Maven para todos

### ¿Por qué 2 etapas separadas para imágenes?

```groovy
stage('Build & Push Infrastructure Images') { ... }
stage('Build & Push Business Services Images') { ... }
```

- **Separación lógica**: Distingue infraestructura de negocio
- **Orden de despliegue**: La infraestructura debe desplegarse primero
- **Claridad**: Fácil identificar qué tipo de servicio está construyéndose

---

## 🚦 Próximos Pasos (Punto 3+)

Este pipeline está listo para:

1. **Integración con Kubernetes**: Las imágenes en Docker Hub pueden desplegarse con `kubectl` o Helm
2. **Tests de Integración**: Añadir stage para probar comunicación entre servicios
3. **Despliegue automático**: Añadir stage para deploy a cluster K8s
4. **Monitoring**: Integrar con Prometheus/Grafana

---

## 📞 Contacto y Soporte

- **Repository**: https://github.com/SelimHorri/ecommerce-microservice-backend-app/
- **Docker Hub**: https://hub.docker.com/u/valentapi16
- **Branch**: develop

---

## 📊 Métricas del Pipeline

| Métrica | Valor Estimado |
|---------|----------------|
| Tiempo de compilación | ~5-8 min |
| Tiempo de tests | ~2-3 min |
| Tiempo de build Docker | ~10-15 min |
| Tiempo total del pipeline | ~20-30 min |
| Número de imágenes generadas | 18 (9 servicios × 2 tags) |

---

## 🎓 Justificación Académica

### Selección de Microservicios

Los 6 microservicios funcionales fueron seleccionados considerando:

1. **Cobertura de dominio**: Representan los principales módulos del e-commerce
   - Usuarios (user-service)
   - Productos (product-service)
   - Órdenes (order-service)
   - Pagos (payment-service)
   - Envíos (shipping-service)
   - Favoritos (favourite-service)

2. **Comunicación inter-servicios**: Cada servicio tiene al menos una dependencia con otro
   - Permite pruebas de integración realistas
   - Demuestra patrones de comunicación síncrona (RestTemplate)

3. **Complejidad gradual**: Mezcla de servicios simples (product, user) y complejos (order, shipping)

### Infraestructura Incluida

Aunque el ejercicio pide "al menos 6 microservicios", se incluyeron 3 servicios de infraestructura porque:

1. **Necesidad técnica**: Sin service-discovery, los servicios no pueden comunicarse
2. **Best practices**: Cloud-native applications requieren Config Server y API Gateway
3. **Realismo**: Refleja arquitectura de producción real

---

**Fecha de creación**: 29 de octubre de 2025  
**Versión**: 1.0  
**Autor**: Pipeline configurado para cumplir requisitos del Punto 2
