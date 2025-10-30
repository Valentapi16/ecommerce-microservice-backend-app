#!/bin/bash

# Health Check Script para Kubernetes
# Verifica el estado de salud de todos los servicios desplegados

NAMESPACE=$1

if [ -z "$NAMESPACE" ]; then
    echo "Error: Namespace no proporcionado"
    echo "Uso: ./health-check.sh <namespace>"
    exit 1
fi

echo "========================================="
echo "  HEALTH CHECK - Kubernetes Deployment"
echo "  Namespace: $NAMESPACE"
echo "========================================="

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

FAILED=0
TOTAL=0

# Función para verificar health de un servicio
check_service_health() {
    local service=$1
    local port=$2
    
    echo -e "\n🔍 Verificando: $service (puerto $port)"
    TOTAL=$((TOTAL + 1))
    
    # Obtener el pod del servicio
    POD=$(kubectl get pods -n $NAMESPACE -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
    
    if [ -z "$POD" ]; then
        echo -e "${RED}❌ ERROR: No se encontró pod para $service${NC}"
        FAILED=$((FAILED + 1))
        return 1
    fi
    
    # Verificar que el pod esté corriendo
    POD_STATUS=$(kubectl get pod $POD -n $NAMESPACE -o jsonpath='{.status.phase}')
    if [ "$POD_STATUS" != "Running" ]; then
        echo -e "${RED}❌ ERROR: Pod $POD no está en estado Running (estado: $POD_STATUS)${NC}"
        FAILED=$((FAILED + 1))
        return 1
    fi
    
    # Hacer health check al endpoint actuator/health
    HEALTH_CHECK=$(kubectl exec -n $NAMESPACE $POD -- curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health 2>/dev/null || echo "000")
    
    if [ "$HEALTH_CHECK" == "200" ]; then
        echo -e "${GREEN}✅ HEALTHY: $service responde correctamente${NC}"
        
        # Obtener detalles del health check
        HEALTH_DETAILS=$(kubectl exec -n $NAMESPACE $POD -- curl -s http://localhost:$port/actuator/health 2>/dev/null || echo "{}")
        echo "   Detalles: $HEALTH_DETAILS"
        return 0
    else
        echo -e "${RED}❌ UNHEALTHY: $service no responde (HTTP $HEALTH_CHECK)${NC}"
        FAILED=$((FAILED + 1))
        
        # Mostrar logs del pod para debugging
        echo -e "${YELLOW}   Últimos logs del pod:${NC}"
        kubectl logs $POD -n $NAMESPACE --tail=10 2>/dev/null | sed 's/^/   /'
        return 1
    fi
}

# Verificar servicios de infraestructura
echo -e "\n📋 SERVICIOS DE INFRAESTRUCTURA"
echo "================================="
check_service_health "service-discovery" "8761"
check_service_health "cloud-config" "8888"
check_service_health "api-gateway" "8080"

# Verificar servicios de negocio
echo -e "\n💼 SERVICIOS DE NEGOCIO"
echo "======================="
check_service_health "user-service" "8100"
check_service_health "product-service" "8200"
check_service_health "payment-service" "8400"

# Verificar conectividad entre servicios
echo -e "\n🔗 VERIFICACIÓN DE CONECTIVIDAD"
echo "================================"

# Verificar que los servicios se registren en Eureka
echo -e "\n📡 Verificando registro en Eureka..."
DISCOVERY_POD=$(kubectl get pods -n $NAMESPACE -l app=service-discovery -o jsonpath='{.items[0].metadata.name}')
REGISTERED_SERVICES=$(kubectl exec -n $NAMESPACE $DISCOVERY_POD -- curl -s http://localhost:8761/eureka/apps 2>/dev/null | grep -o '<name>[^<]*</name>' | sed 's/<[^>]*>//g' | sort -u)

if [ -z "$REGISTERED_SERVICES" ]; then
    echo -e "${RED}❌ No se pudieron obtener servicios registrados en Eureka${NC}"
    FAILED=$((FAILED + 1))
else
    echo -e "${GREEN}✅ Servicios registrados en Eureka:${NC}"
    echo "$REGISTERED_SERVICES" | while read service; do
        if [ ! -z "$service" ]; then
            echo "   - $service"
        fi
    done
fi

# Resumen final
echo -e "\n========================================="
echo "           RESUMEN DEL HEALTH CHECK"
echo "========================================="
echo "Total de servicios verificados: $TOTAL"
echo "Servicios saludables: $((TOTAL - FAILED))"
echo "Servicios con problemas: $FAILED"

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ TODOS LOS SERVICIOS ESTÁN SALUDABLES${NC}"
    exit 0
else
    echo -e "${RED}❌ HAY SERVICIOS CON PROBLEMAS${NC}"
    echo "Revisa los logs arriba para más detalles"
    exit 1
fi
