# 🚀 Guía Rápida - Jenkins Pipeline

## ⚡ Quick Start

### 1️⃣ Pre-requisitos

```bash
✅ Jenkins instalado y corriendo
✅ Docker instalado
✅ Git configurado
✅ Cuenta de Docker Hub
```

### 2️⃣ Configurar Jenkins

#### Crear Credencial de Docker Hub
1. Jenkins → Manage Jenkins → Manage Credentials
2. Click en "(global)"
3. Add Credentials
4. Configurar:
   - Kind: `Username with password`
   - Username: `valentapi16`
   - Password: `[Tu Docker Hub Token]`
   - ID: `dockerhub` ⚠️ (debe ser exactamente este ID)
   - Description: `Docker Hub credentials`
5. Click "Create"

#### Crear Pipeline Job
1. Jenkins → New Item
2. Nombre: `ecommerce-dev-pipeline` (o el que prefieras)
3. Tipo: `Pipeline`
4. Click OK
5. En la configuración:
   - **Pipeline → Definition**: `Pipeline script from SCM`
   - **SCM**: `Git`
   - **Repository URL**: `https://github.com/SelimHorri/ecommerce-microservice-backend-app/`
   - **Branch**: `*/develop`
   - **Script Path**: `Jenkinsfile`
6. Click "Save"

### 3️⃣ Ejecutar Pipeline

1. En el job, click "Build Now"
2. Ver progreso en "Console Output"
3. Esperar ~20-30 minutos

### 4️⃣ Verificar Resultados

#### En Jenkins
```
✅ Build debe aparecer en verde
✅ Console output debe mostrar "✅ ¡BUILD EXITOSO! 🎉🎉🎉"
✅ Debe listar 18 imágenes Docker creadas
```

#### En Docker Hub
1. Ir a: https://hub.docker.com/u/valentapi16
2. Verificar que aparecen 9 repositorios:
   - service-discovery
   - cloud-config
   - api-gateway
   - product-service
   - user-service
   - payment-service
   - order-service
   - shipping-service
   - favourite-service
3. Cada uno debe tener 2 tags:
   - `dev-{BUILD_NUMBER}`
   - `dev-latest`

---

## 🔧 Comandos Útiles

### Verificar imágenes localmente
```bash
# Listar imágenes Docker generadas
docker images | grep valentapi16

# Pull de una imagen específica
docker pull valentapi16/user-service:dev-latest

# Ejecutar un servicio localmente (ejemplo)
docker run -p 8081:8081 valentapi16/user-service:dev-latest
```

### Logs de Jenkins
```bash
# Ver logs del último build
curl -u user:token http://jenkins-url/job/ecommerce-dev-pipeline/lastBuild/consoleText

# O desde la interfaz web
# Jenkins → Job → Build Number → Console Output
```

---

## 📊 Monitoreo del Pipeline

### Durante la ejecución

```
Stage View en Jenkins muestra:
┌──────────────────────────┐
│ 1. Checkout         ✅   │  ~30s
├──────────────────────────┤
│ 2. Build All Services ⏳ │  5-8 min
├──────────────────────────┤
│ 3. Run Tests         ⏳  │  2-3 min
├──────────────────────────┤
│ 4. Docker Login      ⏳  │  10s
├──────────────────────────┤
│ 5. Build Infra       ⏳  │  5-8 min
├──────────────────────────┤
│ 6. Build Services    ⏳  │  8-12 min
├──────────────────────────┤
│ 7. Post-Build        ⏳  │  10s
└──────────────────────────┘
```

---

## ❌ Troubleshooting

### Error: "Cannot connect to the Docker daemon"
```bash
# Solución: Verificar que Docker está corriendo
systemctl status docker
# O en Windows/Mac: Abrir Docker Desktop
```

### Error: "dockerhub credentials not found"
```bash
# Solución: Verificar que la credencial existe y tiene el ID correcto
# Jenkins → Manage Credentials → verificar ID = "dockerhub"
```

### Error: "mvn: command not found"
```bash
# Solución: El pipeline usa Docker para compilar, no necesita Maven local
# Verificar que Docker está instalado y corriendo
```

### Error: "Permission denied while trying to connect to Docker"
```bash
# Solución: Agregar usuario de Jenkins al grupo docker
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### Build tarda mucho
```bash
# Normal: Primera ejecución tarda más (descarga imágenes base)
# Ejecuciones posteriores: ~20-30 min
# Optimización futura: Implementar caché de dependencias Maven
```

### Algunos tests fallan pero build continúa
```bash
# Normal en DEV: El pipeline tiene tolerancia a fallos en tests
# Ver logs específicos en: order-service/target/surefire-reports/
```

---

## 🎯 Verificación Rápida de Comunicación

### 1. Verificar que los servicios usan Eureka

```bash
# Buscar configuración de Eureka en cada servicio
grep -r "eureka.client.service-url" */src/main/resources/
```

### 2. Verificar RestTemplate

```bash
# Buscar uso de RestTemplate en los servicios
grep -r "RestTemplate" */src/main/java/ | grep -v ".class"
```

### 3. Verificar URLs de comunicación

```bash
# Buscar constantes de URLs de servicios
grep -r "USER_SERVICE_API_URL\|PRODUCT_SERVICE_API_URL\|PAYMENT_SERVICE_API_URL" */src/
```

---

## 📦 Estructura de Salida

### Después de un build exitoso:

```
Docker Hub (valentapi16/)
├── service-discovery
│   ├── dev-42
│   └── dev-latest
├── cloud-config
│   ├── dev-42
│   └── dev-latest
├── api-gateway
│   ├── dev-42
│   └── dev-latest
├── product-service
│   ├── dev-42
│   └── dev-latest
├── user-service
│   ├── dev-42
│   └── dev-latest
├── payment-service
│   ├── dev-42
│   └── dev-latest
├── order-service
│   ├── dev-42
│   └── dev-latest
├── shipping-service
│   ├── dev-42
│   └── dev-latest
└── favourite-service
    ├── dev-42
    └── dev-latest

Total: 18 imágenes (9 servicios × 2 tags)
```

---

## 🔄 Flujo de Trabajo Típico

### Desarrollo Local
```bash
1. Hacer cambios en el código
2. Commit y push a branch develop
3. Jenkins detecta cambios automáticamente (si está configurado con webhook)
   O ejecutar manualmente: "Build Now"
4. Esperar a que termine el build
5. Verificar en Docker Hub
6. Listo para desplegar en Kubernetes (Punto 3)
```

### Para hacer cambios al Pipeline
```bash
1. Editar Jenkinsfile en tu repositorio local
2. Commit y push
3. Jenkins usará el nuevo Jenkinsfile en el próximo build
```

---

## 📝 Checklist Pre-Build

Antes de ejecutar el pipeline, verificar:

- [ ] Jenkins está corriendo y accesible
- [ ] Credencial `dockerhub` está configurada correctamente
- [ ] Docker está instalado y corriendo en el nodo de Jenkins
- [ ] Puerto 8080 de Jenkins es accesible
- [ ] Tienes espacio suficiente en disco (~10 GB libres)
- [ ] Conexión a Internet estable (para descargar dependencias)
- [ ] Docker Hub es accesible (no está bloqueado por firewall)

---

## 🎓 Para Presentación Académica

### Evidencia a Capturar:

1. **Screenshot de Jenkins**
   - Stage View mostrando todas las etapas en verde
   - Console Output con el mensaje de éxito

2. **Screenshot de Docker Hub**
   - Lista de repositorios (9 servicios)
   - Tags de un servicio mostrando dev-X y dev-latest

3. **Código fuente**
   - Jenkinsfile
   - Ejemplo de RestTemplate en un servicio
   - AppConstant.java mostrando URLs de servicios

4. **Documentación**
   - JENKINS_PIPELINE_DOCS.md
   - Diagrama de comunicación entre servicios

---

## 📞 Soporte

Si tienes problemas:

1. Revisar Console Output de Jenkins para errores específicos
2. Verificar logs de Docker: `docker logs <container_id>`
3. Consultar documentación detallada: `JENKINS_PIPELINE_DOCS.md`
4. Verificar checklist de cumplimiento: `CHECKLIST_PUNTO_2.md`

---

## 🎉 Éxito!

Si ves esto en Jenkins Console Output:

```
✅ ¡BUILD EXITOSO! 🎉🎉🎉

═══════════════════════════════════════════════════════════
📦 INFRAESTRUCTURA (3 servicios):
═══════════════════════════════════════════════════════════
   ✓ valentapi16/service-discovery:dev-42
   ✓ valentapi16/service-discovery:dev-latest
   ✓ valentapi16/cloud-config:dev-42
   ✓ valentapi16/cloud-config:dev-latest
   ✓ valentapi16/api-gateway:dev-42
   ✓ valentapi16/api-gateway:dev-latest

═══════════════════════════════════════════════════════════
📦 MICROSERVICIOS FUNCIONALES (6 servicios):
═══════════════════════════════════════════════════════════
   ✓ valentapi16/product-service:dev-42
   ✓ valentapi16/product-service:dev-latest
   ...
   (6 servicios más)

🔗 Ver en: https://hub.docker.com/u/valentapi16
═══════════════════════════════════════════════════════════
```

**¡Felicitaciones! El Punto 2 está completo ✅**

---

**Última actualización**: 29 de octubre de 2025  
**Versión**: 1.0  
**Tiempo de lectura**: 5 minutos
