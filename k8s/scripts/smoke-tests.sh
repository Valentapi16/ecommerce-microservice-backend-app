#!/bin/bash

# Smoke Tests Script
# Pruebas rápidas para verificar funcionalidad básica

NAMESPACE=$1

if [ -z "$NAMESPACE" ]; then
    echo "Error: Namespace no proporcionado"
    exit 1
fi

echo "========================================="
echo "   SMOKE TESTS - Quick Verification"
echo "   Namespace: $NAMESPACE"
echo "========================================="

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

PASSED=0
FAILED=0

smoke_test() {
    local test_name=$1
    local command=$2
    
    echo -e "\n💨 $test_name"
    
    if eval "$command" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ PASSED${NC}"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}❌ FAILED${NC}"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

# Test 1: Verificar que todos los pods estén corriendo
smoke_test "Todos los pods Running" \
    "[ \$(kubectl get pods -n $NAMESPACE --field-selector=status.phase=Running --no-headers | wc -l) -ge 6 ]"

# Test 2: Verificar que todos los servicios existan
smoke_test "Todos los services creados" \
    "[ \$(kubectl get svc -n $NAMESPACE --no-headers | wc -l) -ge 6 ]"

# Test 3: Verificar que el ingress exista
smoke_test "Ingress configurado" \
    "kubectl get ingress -n $NAMESPACE | grep -q ecommerce-ingress"

# Test 4: Verificar que service-discovery esté accesible
DISCOVERY_POD=$(kubectl get pods -n $NAMESPACE -l app=service-discovery -o jsonpath='{.items[0].metadata.name}')
smoke_test "Service Discovery accesible" \
    "kubectl exec -n $NAMESPACE $DISCOVERY_POD -- curl -s -f http://localhost:8761/actuator/health > /dev/null"

# Test 5: Verificar que API Gateway esté accesible
GATEWAY_POD=$(kubectl get pods -n $NAMESPACE -l app=api-gateway -o jsonpath='{.items[0].metadata.name}')
smoke_test "API Gateway accesible" \
    "kubectl exec -n $NAMESPACE $GATEWAY_POD -- curl -s -f http://localhost:8080/actuator/health > /dev/null"

# Test 6: Verificar memoria de los pods
smoke_test "Pods dentro de límites de memoria" \
    "[ \$(kubectl top pods -n $NAMESPACE 2>/dev/null | awk 'NR>1 {gsub(\"Mi\",\"\",\$3); if(\$3>900) print}' | wc -l) -eq 0 ]"

echo -e "\n========================================="
echo "         RESUMEN DE SMOKE TESTS"
echo "========================================="
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}✅ SMOKE TESTS EXITOSOS${NC}"
    exit 0
else
    echo -e "\n${RED}❌ SMOKE TESTS FALLIDOS${NC}"
    exit 1
fi
