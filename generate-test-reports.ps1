# 🚀 Script para Generar Reportes de Pruebas
# Ejecuta todas las pruebas y genera reportes HTML profesionales

Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  📊 GENERADOR DE REPORTES - PROYECTO ECOMMERCE" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Limpiar compilaciones anteriores
Write-Host "🧹 Paso 1/4: Limpiando compilaciones anteriores..." -ForegroundColor Yellow
mvn clean
Write-Host "✅ Limpieza completada" -ForegroundColor Green
Write-Host ""

# Paso 2: Ejecutar todas las pruebas
Write-Host "🧪 Paso 2/4: Ejecutando todas las pruebas unitarias..." -ForegroundColor Yellow
mvn test
$testResult = $LASTEXITCODE

if ($testResult -eq 0) {
    Write-Host "✅ Todas las pruebas pasaron exitosamente!" -ForegroundColor Green
} else {
    Write-Host "⚠️  Algunas pruebas fallaron, pero continuando con reportes..." -ForegroundColor Yellow
}
Write-Host ""

# Paso 3: Generar reportes HTML
Write-Host "📊 Paso 3/4: Generando reportes HTML..." -ForegroundColor Yellow
mvn surefire-report:report
mvn site -DgenerateReports=false
Write-Host "✅ Reportes generados exitosamente" -ForegroundColor Green
Write-Host ""

# Paso 4: Mostrar resumen y ubicación de reportes
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  ✅ REPORTES GENERADOS EXITOSAMENTE" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "📁 UBICACIÓN DE LOS REPORTES:" -ForegroundColor White
Write-Host ""
Write-Host "   📊 Reporte Agregado (Todos los servicios):" -ForegroundColor White
Write-Host "      → target\site\surefire-report.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "   📋 Reportes por Servicio:" -ForegroundColor White
Write-Host "      → user-service\target\site\surefire-report.html" -ForegroundColor Cyan
Write-Host "      → product-service\target\site\surefire-report.html" -ForegroundColor Cyan
Write-Host "      → order-service\target\site\surefire-report.html" -ForegroundColor Cyan
Write-Host "      → favourite-service\target\site\surefire-report.html" -ForegroundColor Cyan
Write-Host "      → shipping-service\target\site\surefire-report.html" -ForegroundColor Cyan
Write-Host "      → payment-service\target\site\surefire-report.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan

# Paso 5: Preguntar si desea abrir el reporte
Write-Host ""
$openReport = Read-Host "¿Desea abrir el reporte agregado en el navegador? (S/N)"

if ($openReport -eq "S" -or $openReport -eq "s") {
    Write-Host ""
    Write-Host "🌐 Abriendo reporte en el navegador..." -ForegroundColor Yellow
    Start-Process "target\site\surefire-report.html"
    Write-Host "✅ Reporte abierto" -ForegroundColor Green
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  🎉 PROCESO COMPLETADO" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Tip: Para pruebas de rendimiento con Locust:" -ForegroundColor Yellow
Write-Host "   1. cd performance-tests" -ForegroundColor White
Write-Host "   2. locust -f locustfile.py --host=http://localhost:8080" -ForegroundColor White
Write-Host "   3. Abrir: http://localhost:8089" -ForegroundColor White
Write-Host ""
