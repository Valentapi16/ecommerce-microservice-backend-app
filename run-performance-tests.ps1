# ============================================
# Script para Ejecutar Pruebas de Rendimiento con Locust
# ============================================

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  🚀 PRUEBAS DE RENDIMIENTO - LOCUST" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si Python está instalado
Write-Host "🔍 Verificando Python..." -ForegroundColor Yellow
try {
    $pythonVersion = python --version 2>&1
    Write-Host "   ✅ Python instalado: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Python no está instalado" -ForegroundColor Red
    Write-Host "   📥 Descarga Python desde: https://www.python.org/downloads/" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Verificar si Locust está instalado
Write-Host "🔍 Verificando Locust..." -ForegroundColor Yellow
try {
    $locustVersion = locust --version 2>&1
    Write-Host "   ✅ Locust instalado: $locustVersion" -ForegroundColor Green
} catch {
    Write-Host "   ⚠️  Locust no está instalado" -ForegroundColor Yellow
    Write-Host "   📦 Instalando Locust y dependencias..." -ForegroundColor Yellow
    
    Set-Location performance-tests
    pip install -r requirements.txt
    Set-Location ..
    
    Write-Host "   ✅ Locust instalado exitosamente" -ForegroundColor Green
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  CONFIGURACIÓN DE SERVICIOS" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 URLs de los servicios a probar:" -ForegroundColor White
Write-Host "   • API Gateway:      http://localhost:8080" -ForegroundColor Cyan
Write-Host "   • User Service:     http://localhost:8400" -ForegroundColor Cyan
Write-Host "   • Product Service:  http://localhost:8500" -ForegroundColor Cyan
Write-Host "   • Order Service:    http://localhost:8600" -ForegroundColor Cyan
Write-Host "   • Payment Service:  http://localhost:8700" -ForegroundColor Cyan
Write-Host "   • Shipping Service: http://localhost:8800" -ForegroundColor Cyan
Write-Host "   • Favourite Service:http://localhost:8900" -ForegroundColor Cyan
Write-Host ""

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  SELECCIONA EL SERVICIO A PROBAR" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. API Gateway (Puerto 8080) - Punto de entrada principal" -ForegroundColor White
Write-Host "2. User Service (Puerto 8400) - Gestión de usuarios" -ForegroundColor White
Write-Host "3. Product Service (Puerto 8500) - Gestión de productos" -ForegroundColor White
Write-Host "4. Order Service (Puerto 8600) - Gestión de órdenes" -ForegroundColor White
Write-Host "5. Todos los servicios (Prueba completa E2E)" -ForegroundColor White
Write-Host ""

$selection = Read-Host "Selecciona una opción (1-5)"

$host_url = ""
$test_name = ""

switch ($selection) {
    "1" { 
        $host_url = "http://localhost:8080"
        $test_name = "API Gateway"
    }
    "2" { 
        $host_url = "http://localhost:8400"
        $test_name = "User Service"
    }
    "3" { 
        $host_url = "http://localhost:8500"
        $test_name = "Product Service"
    }
    "4" { 
        $host_url = "http://localhost:8600"
        $test_name = "Order Service"
    }
    "5" { 
        $host_url = "http://localhost:8080"
        $test_name = "E2E Complete Flow"
    }
    default {
        Write-Host "❌ Opción inválida" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  INICIANDO LOCUST" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "🎯 Servicio seleccionado: $test_name" -ForegroundColor Green
Write-Host "🔗 Host: $host_url" -ForegroundColor Green
Write-Host "🌐 Interfaz web: http://localhost:8089" -ForegroundColor Yellow
Write-Host ""
Write-Host "📊 INSTRUCCIONES:" -ForegroundColor Cyan
Write-Host "   1. Se abrirá tu navegador en http://localhost:8089" -ForegroundColor White
Write-Host "   2. Configura el número de usuarios (ej: 10 usuarios)" -ForegroundColor White
Write-Host "   3. Configura la tasa de spawn (ej: 1 usuario/seg)" -ForegroundColor White
Write-Host "   4. Haz clic en 'Start Swarming' para iniciar las pruebas" -ForegroundColor White
Write-Host "   5. Observa las métricas en tiempo real" -ForegroundColor White
Write-Host "   6. Presiona Ctrl+C aquí para detener cuando termines" -ForegroundColor White
Write-Host ""
Write-Host "⏳ Iniciando en 3 segundos..." -ForegroundColor Yellow

Start-Sleep -Seconds 3

# Cambiar al directorio de performance tests
Set-Location performance-tests

# Abrir navegador
Write-Host ""
Write-Host "🌐 Abriendo navegador..." -ForegroundColor Yellow
Start-Process "http://localhost:8089"

Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host "  ✅ LOCUST EJECUTÁNDOSE" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Presiona Ctrl+C para detener las pruebas" -ForegroundColor Yellow
Write-Host ""

# Ejecutar Locust con la configuración seleccionada
# Nota: --web-port 8089 para evitar conflicto con Jenkins en 8080
locust -f locustfile.py --host=$host_url --web-port=8089

Set-Location ..

Write-Host ""
Write-Host "✅ Pruebas de rendimiento finalizadas" -ForegroundColor Green
