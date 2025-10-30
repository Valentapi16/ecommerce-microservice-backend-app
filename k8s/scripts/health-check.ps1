param(
    [Parameter(Mandatory=$true)]
    [string]$Namespace
)

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  HEALTH CHECK - Kubernetes Deployment" -ForegroundColor Cyan
Write-Host "  Namespace: $Namespace" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$Failed = 0
$Total = 0

function Check-ServiceHealth {
    param(
        [string]$Service,
        [int]$Port
    )
    
    Write-Host "`n🔍 Verificando: $Service (puerto $Port)" -ForegroundColor Yellow
    $script:Total++
    
    # Obtener el pod del servicio
    $Pod = kubectl get pods -n $Namespace -l app=$Service -o jsonpath='{.items[0].metadata.name}' 2>$null
    
    if ([string]::IsNullOrEmpty($Pod)) {
        Write-Host "❌ ERROR: No se encontró pod para $Service" -ForegroundColor Red
        $script:Failed++
        return $false
    }
    
    # Verificar que el pod esté corriendo
    $PodStatus = kubectl get pod $Pod -n $Namespace -o jsonpath='{.status.phase}'
    if ($PodStatus -ne "Running") {
        Write-Host "❌ ERROR: Pod $Pod no está en estado Running (estado: $PodStatus)" -ForegroundColor Red
        $script:Failed++
        return $false
    }
    
    # Hacer health check
    $HealthCheck = kubectl exec -n $Namespace $Pod -- curl -s -o /dev/null -w "%{http_code}" http://localhost:$Port/actuator/health 2>$null
    
    if ($HealthCheck -eq "200") {
        Write-Host "✅ HEALTHY: $Service responde correctamente" -ForegroundColor Green
        
        # Obtener detalles
        $HealthDetails = kubectl exec -n $Namespace $Pod -- curl -s http://localhost:$Port/actuator/health 2>$null
        Write-Host "   Detalles: $HealthDetails" -ForegroundColor Gray
        return $true
    }
    else {
        Write-Host "❌ UNHEALTHY: $Service no responde (HTTP $HealthCheck)" -ForegroundColor Red
        $script:Failed++
        
        # Mostrar logs
        Write-Host "   Últimos logs del pod:" -ForegroundColor Yellow
        kubectl logs $Pod -n $Namespace --tail=10 2>$null | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
        return $false
    }
}

# Verificar servicios de infraestructura
Write-Host "`n📋 SERVICIOS DE INFRAESTRUCTURA" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Check-ServiceHealth -Service "service-discovery" -Port 8761
Check-ServiceHealth -Service "cloud-config" -Port 8888
Check-ServiceHealth -Service "api-gateway" -Port 8080

# Verificar servicios de negocio
Write-Host "`n💼 SERVICIOS DE NEGOCIO" -ForegroundColor Cyan
Write-Host "=======================" -ForegroundColor Cyan
Check-ServiceHealth -Service "user-service" -Port 8100
Check-ServiceHealth -Service "product-service" -Port 8200
Check-ServiceHealth -Service "payment-service" -Port 8400

# Verificar registro en Eureka
Write-Host "`n📡 Verificando registro en Eureka..." -ForegroundColor Yellow
$DiscoveryPod = kubectl get pods -n $Namespace -l app=service-discovery -o jsonpath='{.items[0].metadata.name}'
$RegisteredServices = kubectl exec -n $Namespace $DiscoveryPod -- curl -s http://localhost:8761/eureka/apps 2>$null

if ($RegisteredServices) {
    Write-Host "✅ Eureka está respondiendo" -ForegroundColor Green
}
else {
    Write-Host "❌ No se pudo conectar a Eureka" -ForegroundColor Red
    $Failed++
}

# Resumen
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "         RESUMEN DEL HEALTH CHECK" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Total de servicios verificados: $Total"
Write-Host "Servicios saludables: $($Total - $Failed)" -ForegroundColor Green
Write-Host "Servicios con problemas: $Failed" -ForegroundColor Red

if ($Failed -eq 0) {
    Write-Host "`n✅ TODOS LOS SERVICIOS ESTÁN SALUDABLES" -ForegroundColor Green
    exit 0
}
else {
    Write-Host "`n❌ HAY SERVICIOS CON PROBLEMAS" -ForegroundColor Red
    exit 1
}
