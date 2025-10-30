param(
    [Parameter(Mandatory=$true)]
    [string]$Namespace
)

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "   INTEGRATION TESTS - E2E" -ForegroundColor Cyan
Write-Host "   Namespace: $Namespace" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$Passed = 0
$Failed = 0

# Obtener la URL del Ingress
$IngressHost = kubectl get ingress -n $Namespace -o jsonpath='{.items[0].spec.rules[0].host}'
if ([string]::IsNullOrEmpty($IngressHost)) {
    $IngressHost = "localhost"
}

Write-Host "API Gateway: $IngressHost" -ForegroundColor Yellow

function Run-Test {
    param(
        [string]$TestName,
        [string]$Method,
        [string]$Endpoint,
        [int]$ExpectedStatus,
        [string]$Data = ""
    )
    
    Write-Host "`n🧪 Test: $TestName" -ForegroundColor Yellow
    
    try {
        if ($Method -eq "GET") {
            $Response = Invoke-WebRequest -Uri "http://$IngressHost$Endpoint" -Method GET -UseBasicParsing -ErrorAction Stop
        }
        elseif ($Method -eq "POST") {
            $Response = Invoke-WebRequest -Uri "http://$IngressHost$Endpoint" -Method POST -Body $Data -ContentType "application/json" -UseBasicParsing -ErrorAction Stop
        }
        
        if ($Response.StatusCode -eq $ExpectedStatus) {
            Write-Host "✅ PASSED: HTTP $($Response.StatusCode) (esperado: $ExpectedStatus)" -ForegroundColor Green
            $script:Passed++
            return $true
        }
        else {
            Write-Host "❌ FAILED: HTTP $($Response.StatusCode) (esperado: $ExpectedStatus)" -ForegroundColor Red
            $script:Failed++
            return $false
        }
    }
    catch {
        Write-Host "❌ FAILED: Error en la petición - $($_.Exception.Message)" -ForegroundColor Red
        $script:Failed++
        return $false
    }
}

# Tests de User Service
Write-Host "`n👤 USER SERVICE TESTS" -ForegroundColor Cyan
Write-Host "====================" -ForegroundColor Cyan
Run-Test -TestName "Health check - User Service" -Method "GET" -Endpoint "/api/users/actuator/health" -ExpectedStatus 200
Run-Test -TestName "Get all users" -Method "GET" -Endpoint "/api/users" -ExpectedStatus 200

# Tests de Product Service
Write-Host "`n📦 PRODUCT SERVICE TESTS" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan
Run-Test -TestName "Health check - Product Service" -Method "GET" -Endpoint "/api/products/actuator/health" -ExpectedStatus 200
Run-Test -TestName "Get all products" -Method "GET" -Endpoint "/api/products" -ExpectedStatus 200

# Tests de Payment Service
Write-Host "`n💳 PAYMENT SERVICE TESTS" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan
Run-Test -TestName "Health check - Payment Service" -Method "GET" -Endpoint "/api/payments/actuator/health" -ExpectedStatus 200
Run-Test -TestName "Get all payments" -Method "GET" -Endpoint "/api/payments" -ExpectedStatus 200

# Test API Gateway
Write-Host "`n🔄 API GATEWAY TEST" -ForegroundColor Cyan
Write-Host "===================" -ForegroundColor Cyan
Run-Test -TestName "API Gateway Health" -Method "GET" -Endpoint "/actuator/health" -ExpectedStatus 200

# Resumen
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "       RESUMEN DE INTEGRATION TESTS" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Total de tests: $($Passed + $Failed)"
Write-Host "Tests pasados: $Passed" -ForegroundColor Green
Write-Host "Tests fallidos: $Failed" -ForegroundColor Red

if ($Failed -eq 0) {
    Write-Host "`n✅ TODOS LOS TESTS DE INTEGRACIÓN PASARON" -ForegroundColor Green
    exit 0
}
else {
    Write-Host "`n❌ ALGUNOS TESTS FALLARON" -ForegroundColor Red
    exit 1
}
