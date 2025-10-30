# 🚀 Script para Iniciar Locust con Configuración Óptima
# Ejecuta pruebas de rendimiento con interfaz web

Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  🐝 LOCUST - PRUEBAS DE RENDIMIENTO" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Verificar si estamos en la carpeta correcta
if (-Not (Test-Path "locustfile.py")) {
    Write-Host "❌ Error: locustfile.py no encontrado" -ForegroundColor Red
    Write-Host "   Por favor ejecuta este script desde: performance-tests/" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Presiona Enter para salir"
    exit 1
}

# Verificar si Locust está instalado
Write-Host "🔍 Verificando instalación de Locust..." -ForegroundColor Yellow
$locustInstalled = python -m pip show locust 2>$null

if (-Not $locustInstalled) {
    Write-Host "❌ Locust no está instalado" -ForegroundColor Red
    Write-Host ""
    $install = Read-Host "¿Desea instalar Locust ahora? (S/N)"
    
    if ($install -eq "S" -or $install -eq "s") {
        Write-Host ""
        Write-Host "📦 Instalando dependencias..." -ForegroundColor Yellow
        pip install -r requirements.txt
        Write-Host "✅ Instalación completada" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "Para instalar manualmente ejecuta:" -ForegroundColor Yellow
        Write-Host "   pip install -r requirements.txt" -ForegroundColor White
        Write-Host ""
        Read-Host "Presiona Enter para salir"
        exit 1
    }
}

Write-Host "✅ Locust está instalado" -ForegroundColor Green
Write-Host ""

# Crear carpeta de reportes si no existe
if (-Not (Test-Path "reports")) {
    New-Item -ItemType Directory -Path "reports" | Out-Null
    Write-Host "✅ Carpeta 'reports' creada" -ForegroundColor Green
}

# Preguntar por el host
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  ⚙️  CONFIGURACIÓN" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Hosts disponibles:" -ForegroundColor White
Write-Host "  1. http://localhost:8080 (API Gateway local)" -ForegroundColor Cyan
Write-Host "  2. Otro host" -ForegroundColor Cyan
Write-Host ""
$hostOption = Read-Host "Selecciona una opción (1-2)"

$host_url = "http://localhost:8080"
if ($hostOption -eq "2") {
    $host_url = Read-Host "Ingresa el host (ej: http://localhost:8080)"
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  🚀 INICIANDO LOCUST" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Configuración:" -ForegroundColor White
Write-Host "   Host:     $host_url" -ForegroundColor Cyan
Write-Host "   Web UI:   http://localhost:8089" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Instrucciones:" -ForegroundColor Yellow
Write-Host "   1. Espera a que se abra el navegador" -ForegroundColor White
Write-Host "   2. Configura número de usuarios (ej: 100)" -ForegroundColor White
Write-Host "   3. Configura spawn rate (ej: 10 usuarios/seg)" -ForegroundColor White
Write-Host "   4. Click en 'Start Swarming' 🐝" -ForegroundColor White
Write-Host "   5. Observa métricas en tiempo real" -ForegroundColor White
Write-Host ""
Write-Host "⚠️  Para detener: Presiona Ctrl+C en esta terminal" -ForegroundColor Yellow
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan

# Esperar confirmación
$continue = Read-Host "¿Continuar? (S/N)"
if ($continue -ne "S" -and $continue -ne "s") {
    Write-Host "❌ Operación cancelada" -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "🐝 Iniciando Locust..." -ForegroundColor Green
Write-Host ""

# Esperar 2 segundos antes de abrir navegador
Start-Sleep -Seconds 2

# Abrir navegador en segundo plano
Start-Process "http://localhost:8089"

# Iniciar Locust
locust -f locustfile.py --host=$host_url

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  👋 LOCUST DETENIDO" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Tip: Puedes descargar reportes desde la interfaz web" -ForegroundColor Yellow
Write-Host "   o ejecutar en modo headless para generar HTML:" -ForegroundColor Yellow
Write-Host ""
Write-Host "   locust -f locustfile.py --host=$host_url \" -ForegroundColor White
Write-Host "     --users=100 --spawn-rate=10 --run-time=2m \" -ForegroundColor White
Write-Host "     --headless --html=reports/locust_report.html" -ForegroundColor White
Write-Host ""
