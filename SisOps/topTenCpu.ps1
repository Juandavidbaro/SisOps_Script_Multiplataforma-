$rutaArchivo = "cpuLog.txt"
$intervalo = 500
$totalCpuPorProceso = @{}

while ($true) {
    $procesos = Get-Process

    foreach ($proceso in $procesos) {
        $nombreProceso = $proceso.ProcessName

        $cpuUsada = $proceso.CPU

        if ($totalCpuPorProceso.ContainsKey($nombreProceso)) {
            $totalCpuPorProceso[$nombreProceso] += $cpuUsada
        } else {
            $totalCpuPorProceso[$nombreProceso] = $cpuUsada
        }
    }

    Set-Content -Path $rutaArchivo -Value ""
    $totalCpuPorProceso.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 10 | ForEach-Object {
        Add-Content -Path $rutaArchivo -Value "$($_.Name): $($_.Value) segundos"
    }

    Start-Sleep -Milliseconds $intervalo
}