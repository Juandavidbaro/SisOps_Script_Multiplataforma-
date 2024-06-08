param (
    [int]$pidSeleccionado
)
$intervalo = 100
$veces = 10 * 30 * 1
$totalMemoriaPorProceso = 0

$usoMemoriaMin = [math]::Infinity
$usoMemoriaMax = 0
$sumaMemoria = 0
$conteoMuestras = 0

for ($i = 0; $i -lt $veces; $i++) {
    $proceso = Get-Process -Id $pidSeleccionado -ErrorAction SilentlyContinue

    if ($null -eq $proceso) {
        Write-Host "El proceso con PID $pidSeleccionado no se encuentra."
        break
    }

    $memoriaUsada = $proceso.WorkingSet64 / 1MB
    $totalMemoriaPorProceso += $memoriaUsada
    $usoMemoriaMin = [math]::Min($usoMemoriaMin, $memoriaUsada)
    $usoMemoriaMax = [math]::Max($usoMemoriaMax, $memoriaUsada)
    $sumaMemoria += $memoriaUsada
    $conteoMuestras++

    Start-Sleep -Milliseconds $intervalo
}

if ($conteoMuestras -gt 0) {
    $usoMemoriaPromedio = $sumaMemoria / $conteoMuestras
} else {
    $usoMemoriaPromedio = 0
}

Write-Host  "Uso de memoria para proceso con PID $pidSeleccionado"
Write-Host  "Uso minimo de memoria: $usoMemoriaMin MB"
Write-Host  "Uso maximo de memoria: $usoMemoriaMax MB"
Write-Host  "Uso promedio de memoria: $usoMemoriaPromedio MB"
