param(
    [Parameter(Mandatory=$true)]
    [string]$Namespace
)

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "   SMOKE TESTS - Quick Verification" -ForegroundColor Cyan
Write-Host "   Namespace: $Namespace" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$Passed = 0
$Failed = 0

function Smoke-Test {
    param(
        [string]$TestName,
        [scriptblock]$TestCommand
    )
    
    Write-Host "`n💨 $TestName" -ForegroundColor Yellow
    
    try {
        $result = & $TestCommand
        if ($result) {
            Write-Host "✅ PASSED" -ForegroundColor Green
            $script:Passed++
            return $true
        }
        else {
            Write-Host "❌ FAILED" -ForegroundColor Red
            $script:Failed++
            return $false
        }
    }
    catch {
        Write-Host "❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
        $script:Failed++
        return $false
    }
}

# Test 1: Verificar que todos los pods estén corriendo
Smoke-Test -TestName "Todos los pods Running" -TestCommand {
    $runningPods = (kubectl get pods -n $Namespace --field-selector=status.phase=Running --no-headers | Measure-Object).Count
    return $runningPods -ge 6
}

# Test 2: Verificar que todos los servicios existan
Smoke-Test -TestName "Todos los services creados" -TestCommand {
    $services = (kubectl get svc -n $Namespace --no-headers | Measure-Object).Count
    return $services -ge 6
}

# Test 3: Verificar que el ingress exista
Smoke-Test -TestName "Ingress configurado" -TestCommand {
    $ingress = kubectl get ingress -n $Namespace 2>$null | Select-String "ecommerce-ingress"
    return $null -ne $ingress
}

# Test 4: Verificar que service-discovery esté accesible
Smoke-Test -TestName "Service Discovery accesible" -TestCommand {
    $pod = kubectl get pods -n $Namespace -l app=service-discovery -o jsonpath='{.items[0].metadata.name}'
    if ($pod) {
        $health = kubectl exec -n $Namespace $pod -- curl -s -f http://localhost:8761/actuator/health 2>$null
        return $null -ne $health
    }
    return $false
}

# Test 5: Verificar que API Gateway esté accesible
Smoke-Test -TestName "API Gateway accesible" -TestCommand {
    $pod = kubectl get pods -n $Namespace -l app=api-gateway -o jsonpath='{.items[0].metadata.name}'
    if ($pod) {
        $health = kubectl exec -n $Namespace $pod -- curl -s -f http://localhost:8080/actuator/health 2>$null
        return $null -ne $health
    }
    return $false
}

# Test 6: Verificar deployments
Smoke-Test -TestName "Deployments listos" -TestCommand {
    $deployments = kubectl get deployments -n $Namespace -o jsonpath='{.items[*].status.conditions[?(@.type=="Available")].status}' 2>$null
    $allTrue = ($deployments -split " ") | Where-Object { $_ -ne "True" }
    return $allTrue.Count -eq 0
}

# Resumen
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "       RESUMEN DE SMOKE TESTS" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Passed: $Passed" -ForegroundColor Green
Write-Host "Failed: $Failed" -ForegroundColor Red

if ($Failed -eq 0) {
    Write-Host "`n✅ SMOKE TESTS EXITOSOS" -ForegroundColor Green
    exit 0
}
else {
    Write-Host "`n❌ SMOKE TESTS FALLIDOS" -ForegroundColor Red
    exit 1
}
