param (
    [int]$pidSeleccionado
)
$intervalo = 100
$veces = 10 * 30 * 1
$totalCpuPorProceso = 0

$usoCpuMin = [math]::Infinity
$usoCpuMax = 0
$sumaCpu = 0
$conteoMuestras = 0

for ($i = 0; $i -lt $veces; $i++) {
    $proceso = Get-Process -Id $pidSeleccionado -ErrorAction SilentlyContinue

    if ($null -eq $proceso) {
        Write-Host "El proceso con PID $PID no se encuentra."
        break
    }

    $cpuUsada = $proceso.CPU
    $totalCpuPorProceso += $cpuUsada
    $usoCpuMin = [math]::Min($usoCpuMin, $cpuUsada)
    $usoCpuMax = [math]::Max($usoCpuMax, $cpuUsada)
    $sumaCpu += $cpuUsada
    $conteoMuestras++

    Start-Sleep -Milliseconds $intervalo
}

if ($conteoMuestras -gt 0) {
    $usoCpuPromedio = $sumaCpu / $conteoMuestras
} else {
    $usoCpuPromedio = 0
}

Write-Host  "Uso de CPU para proceso con PID $pidSeleccionado"
Write-Host  "Uso minimo de CPU: $usoCpuMin segundos"
Write-Host "Uso maximo de CPU: $usoCpuMax segundos"
Write-Host  "Uso promedio de CPU: $usoCpuPromedio segundos"