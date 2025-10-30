# Pipeline de Jenkins - Visualización

## 🏗️ Arquitectura de Microservicios Seleccionados

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                  JENKINS PIPELINE                                │
│                              (DEV Environment Build)                             │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                    ┌───────────────────┴──────────────────┐
                    │                                       │
           ┌────────▼────────┐                    ┌────────▼────────┐
           │ INFRASTRUCTURE  │                    │    BUSINESS     │
           │   (3 services)  │                    │  (6 services)   │
           └────────┬────────┘                    └────────┬────────┘
                    │                                      │
        ┌───────────┼──────────┐          ┌──────────┬────┴────┬──────────┐
        │           │          │          │          │         │          │
        ▼           ▼          ▼          ▼          ▼         ▼          ▼
┌──────────┐ ┌──────────┐ ┌─────────┐ ┌────────┐ ┌─────┐ ┌────────┐ ┌─────────┐
│ service- │ │  cloud-  │ │   api-  │ │product-│ │user-│ │payment-│ │ order-  │
│discovery │ │ config   │ │ gateway │ │service │ │serv.│ │service │ │ service │
│          │ │          │ │         │ │        │ │     │ │        │ │         │
│  :8761   │ │  :9296   │ │  :8080  │ │ :8082  │ │:8081│ │ :8084  │ │  :8083  │
└────┬─────┘ └────┬─────┘ └────┬────┘ └───┬────┘ └──┬──┘ └───┬────┘ └────┬────┘
     │            │             │          │         │        │           │
     └────────────┴─────────────┴──────────┴─────────┴────────┴───────────┘
                                                                            │
                                                              ┌─────────────┴────────┐
                                                              │                      │
                                                              ▼                      ▼
                                                      ┌──────────────┐      ┌─────────────┐
                                                      │  shipping-   │      │  favourite- │
                                                      │   service    │      │   service   │
                                                      │    :8085     │      │    :8086    │
                                                      └──────────────┘      └─────────────┘
```

## 📊 Pipeline Flow (Etapas)

```
START
  │
  ▼
┌─────────────────────────────────────┐
│  1. CHECKOUT                        │
│  └─> git clone + branch develop    │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  2. BUILD (Maven)                   │
│  └─> Compilar todos los servicios  │
│  └─> Extraer JARs                  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  3. RUN TESTS                       │
│  └─> mvn test                       │
│  └─> Tolerancia a fallos (DEV)     │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  4. DOCKER LOGIN                    │
│  └─> Autenticar en Docker Hub      │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  5. BUILD INFRASTRUCTURE IMAGES     │
│  ├─> service-discovery              │
│  ├─> cloud-config                   │
│  └─> api-gateway                    │
│      └─> Push to Docker Hub         │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  6. BUILD BUSINESS SERVICES IMAGES  │
│  ├─> product-service                │
│  ├─> user-service                   │
│  ├─> payment-service                │
│  ├─> order-service                  │
│  ├─> shipping-service               │
│  └─> favourite-service              │
│      └─> Push to Docker Hub         │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  7. POST-BUILD                      │
│  ├─> Docker logout                  │
│  ├─> Reportar resultados            │
│  └─> Listar imágenes creadas        │
└──────────────┬──────────────────────┘
               │
               ▼
             END
```

## 🔗 Comunicación Entre Servicios (Service Mesh)

```
┌────────────────────────────────────────────────────────────────┐
│                     SERVICE DISCOVERY (EUREKA)                  │
│                   Registro y descubrimiento                     │
└───────────────────────────┬────────────────────────────────────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             ▼             ▼
        ┌──────────┐  ┌──────────┐  ┌──────────┐
        │  USER    │  │ PRODUCT  │  │ PAYMENT  │
        │ SERVICE  │  │ SERVICE  │  │ SERVICE  │
        │ (BASE)   │  │ (BASE)   │  │ (BASE)   │
        └────┬─────┘  └────┬─────┘  └────┬─────┘
             │             │              │
             │ ◄───────────┼──────────────┘
             │             │
             └─────────────┤
                           │
                     ┌─────▼─────┐
                     │   ORDER   │
                     │  SERVICE  │
                     │ (CONSUMER)│
                     └─────┬─────┘
                           │
              ┌────────────┴─────────────┐
              │                          │
              ▼                          ▼
       ┌────────────┐            ┌─────────────┐
       │ SHIPPING   │            │  FAVOURITE  │
       │  SERVICE   │            │   SERVICE   │
       │ (CONSUMER) │            │  (CONSUMER) │
       └────────────┘            └─────────────┘

Legend:
  ─►  : Synchronous REST call (RestTemplate)
  BASE: Service that provides data
  CONSUMER: Service that calls other services
```

## 📦 Docker Images Generadas

```
Docker Hub Repository: valentapi16/
│
├── service-discovery:dev-${BUILD_NUMBER}
├── service-discovery:dev-latest
│
├── cloud-config:dev-${BUILD_NUMBER}
├── cloud-config:dev-latest
│
├── api-gateway:dev-${BUILD_NUMBER}
├── api-gateway:dev-latest
│
├── product-service:dev-${BUILD_NUMBER}
├── product-service:dev-latest
│
├── user-service:dev-${BUILD_NUMBER}
├── user-service:dev-latest
│
├── payment-service:dev-${BUILD_NUMBER}
├── payment-service:dev-latest
│
├── order-service:dev-${BUILD_NUMBER}
├── order-service:dev-latest
│
├── shipping-service:dev-${BUILD_NUMBER}
├── shipping-service:dev-latest
│
└── favourite-service:dev-${BUILD_NUMBER}
    └── favourite-service:dev-latest

Total: 18 imágenes (9 servicios × 2 tags)
```

## 🎯 Matriz de Comunicación

| Servicio          | Puerto | Consume                       | Es consumido por          | Tipo    |
|-------------------|--------|-------------------------------|---------------------------|---------|
| user-service      | 8081   | -                             | order, favourite          | BASE    |
| product-service   | 8082   | -                             | order, shipping, favourite| BASE    |
| payment-service   | 8084   | -                             | order                     | BASE    |
| order-service     | 8083   | user, product, payment        | shipping                  | HYBRID  |
| shipping-service  | 8085   | product, order                | -                         | CONSUMER|
| favourite-service | 8086   | user, product                 | -                         | CONSUMER|
| service-discovery | 8761   | (Todos se registran aquí)     | (Provee discovery)        | INFRA   |
| cloud-config      | 9296   | (Todos obtienen config)       | (Provee config)           | INFRA   |
| api-gateway       | 8080   | (Enruta a todos los servicios)| (Entry point)             | INFRA   |

## 🔄 Orden de Despliegue Recomendado (Kubernetes)

```
FASE 1: INFRAESTRUCTURA
┌─────────────────────────┐
│ 1. service-discovery    │  ◄── PRIMERO (otros se registran aquí)
└─────────────────────────┘
           │
           ▼
┌─────────────────────────┐
│ 2. cloud-config         │  ◄── SEGUNDO (otros obtienen configuración)
└─────────────────────────┘
           │
           ▼
┌─────────────────────────┐
│ 3. api-gateway          │  ◄── TERCERO (punto de entrada)
└─────────────────────────┘

FASE 2: SERVICIOS BASE
┌─────────────────────────┐
│ 4. user-service         │  ◄── Sin dependencias de negocio
└─────────────────────────┘
┌─────────────────────────┐
│ 5. product-service      │  ◄── Sin dependencias de negocio
└─────────────────────────┘
┌─────────────────────────┐
│ 6. payment-service      │  ◄── Sin dependencias de negocio
└─────────────────────────┘

FASE 3: SERVICIOS CONSUMIDORES
┌─────────────────────────┐
│ 7. order-service        │  ◄── Depende de: user, product, payment
└─────────────────────────┘
┌─────────────────────────┐
│ 8. shipping-service     │  ◄── Depende de: product, order
└─────────────────────────┘
┌─────────────────────────┐
│ 9. favourite-service    │  ◄── Depende de: user, product
└─────────────────────────┘
```

## 📈 Gráfico de Dependencias

```
                    ┌──────────────────────────────────────┐
                    │      INFRASTRUCTURE LAYER            │
                    │                                      │
                    │  ┌───────────────────────────────┐  │
                    │  │   Service Discovery (Eureka)  │  │
                    │  └───────────────────────────────┘  │
                    │  ┌───────────────────────────────┐  │
                    │  │   Config Server              │  │
                    │  └───────────────────────────────┘  │
                    │  ┌───────────────────────────────┐  │
                    │  │   API Gateway                │  │
                    │  └───────────────────────────────┘  │
                    └──────────────────────────────────────┘
                                     │
                                     │
                    ┌────────────────┴───────────────────┐
                    │                                    │
                    ▼                                    ▼
        ┌──────────────────────┐          ┌──────────────────────┐
        │   DATA PROVIDERS     │          │    CONSUMERS         │
        │   (BASE SERVICES)    │          │                      │
        │                      │          │                      │
        │  • user-service      │──────────┤  • order-service     │
        │  • product-service   │──────────┤  • shipping-service  │
        │  • payment-service   │──────────┤  • favourite-service │
        │                      │          │                      │
        └──────────────────────┘          └──────────────────────┘
                 │                                     │
                 │                                     │
                 └──────────────┬──────────────────────┘
                                │
                                ▼
                    ┌──────────────────────┐
                    │   PERSISTENT LAYER   │
                    │   (Databases)        │
                    │   • MySQL (prod)     │
                    │   • H2 (dev)         │
                    └──────────────────────┘
```

## ⚙️ Configuración de Jenkins (Credenciales Requeridas)

```
Jenkins → Manage Credentials
│
└── Global Credentials
    │
    └── dockerhub (Username with password)
        ├── ID: dockerhub
        ├── Username: valentapi16
        └── Password: [Docker Hub Token]
```

## 🧪 Comandos de Verificación Post-Pipeline

```bash
# Verificar imágenes en Docker Hub
docker search valentapi16

# Pull y ejecutar localmente
docker pull valentapi16/user-service:dev-latest
docker pull valentapi16/product-service:dev-latest
docker pull valentapi16/order-service:dev-latest

# Ver logs de build en Jenkins
curl -u user:token http://jenkins-url/job/pipeline-name/lastBuild/consoleText
```
