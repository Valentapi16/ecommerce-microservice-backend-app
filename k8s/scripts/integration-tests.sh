#!/bin/bash

# Integration Tests Script para Kubernetes
# Ejecuta pruebas de integración end-to-end en el ambiente staging

API_URL=$1

if [ -z "$API_URL" ]; then
    echo "Error: URL del API Gateway no proporcionada"
    echo "Uso: ./integration-tests.sh <api-url>"
    exit 1
fi

echo "========================================="
echo "   INTEGRATION TESTS - E2E"
echo "   API Gateway: $API_URL"
echo "========================================="

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASSED=0
FAILED=0

# Función para ejecutar test
run_test() {
    local test_name=$1
    local method=$2
    local endpoint=$3
    local expected_status=$4
    local data=$5
    
    echo -e "\n🧪 Test: $test_name"
    
    if [ "$method" == "GET" ]; then
        RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://$API_URL$endpoint)
    elif [ "$method" == "POST" ]; then
        RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" http://$API_URL$endpoint)
    fi
    
    if [ "$RESPONSE" == "$expected_status" ]; then
        echo -e "${GREEN}✅ PASSED: HTTP $RESPONSE (esperado: $expected_status)${NC}"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}❌ FAILED: HTTP $RESPONSE (esperado: $expected_status)${NC}"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

# Tests de User Service
echo -e "\n👤 USER SERVICE TESTS"
echo "===================="
run_test "Health check - User Service" "GET" "/api/users/actuator/health" "200"
run_test "Get all users" "GET" "/api/users" "200"

# Tests de Product Service
echo -e "\n📦 PRODUCT SERVICE TESTS"
echo "========================"
run_test "Health check - Product Service" "GET" "/api/products/actuator/health" "200"
run_test "Get all products" "GET" "/api/products" "200"

# Tests de Payment Service
echo -e "\n💳 PAYMENT SERVICE TESTS"
echo "========================"
run_test "Health check - Payment Service" "GET" "/api/payments/actuator/health" "200"
run_test "Get all payments" "GET" "/api/payments" "200"

# Test E2E completo: Crear usuario, producto y pago
echo -e "\n🔄 END-TO-END INTEGRATION TEST"
echo "=============================="

# 1. Crear usuario
USER_DATA='{"username":"testuser","email":"test@example.com","password":"Test123!"}'
echo "1️⃣ Creando usuario de prueba..."
USER_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d "$USER_DATA" http://$API_URL/api/users)
echo "Respuesta: $USER_RESPONSE"

# 2. Crear producto
PRODUCT_DATA='{"title":"Test Product","price":99.99,"quantity":10}'
echo "2️⃣ Creando producto de prueba..."
PRODUCT_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d "$PRODUCT_DATA" http://$API_URL/api/products)
echo "Respuesta: $PRODUCT_RESPONSE"

# 3. Verificar comunicación entre servicios via API Gateway
echo "3️⃣ Verificando comunicación entre servicios..."
GATEWAY_TEST=$(curl -s -o /dev/null -w "%{http_code}" http://$API_URL/actuator/health)
if [ "$GATEWAY_TEST" == "200" ]; then
    echo -e "${GREEN}✅ API Gateway funcionando correctamente${NC}"
    PASSED=$((PASSED + 1))
else
    echo -e "${RED}❌ API Gateway no responde${NC}"
    FAILED=$((FAILED + 1))
fi

# Resumen
echo -e "\n========================================="
echo "         RESUMEN DE INTEGRATION TESTS"
echo "========================================="
echo "Total de tests: $((PASSED + FAILED))"
echo -e "${GREEN}Tests pasados: $PASSED${NC}"
echo -e "${RED}Tests fallidos: $FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}✅ TODOS LOS TESTS DE INTEGRACIÓN PASARON${NC}"
    exit 0
else
    echo -e "\n${RED}❌ ALGUNOS TESTS FALLARON${NC}"
    exit 1
fi
