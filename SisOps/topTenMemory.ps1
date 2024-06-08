$rutaArchivo = "memoryLog.txt"
$intervalo = 500
$totalRAMPorProceso = @{}

while ($true) {
    $procesos = Get-Process

    foreach ($proceso in $procesos) {
        $nombreProceso = $proceso.ProcessName

        $ramUsada = $proceso.WorkingSet / 1MB

        if ($totalRAMPorProceso.ContainsKey($nombreProceso)) {
            $totalRAMPorProceso[$nombreProceso] += $ramUsada
        } else {
            $totalRAMPorProceso[$nombreProceso] = $ramUsada
        }
    }

    Set-Content -Path $rutaArchivo -Value ""
    $totalRAMPorProceso.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 10 | ForEach-Object {
        Add-Content -Path $rutaArchivo -Value "$($_.Name): $($_.Value) MB"
    }

    Start-Sleep -Milliseconds $intervalo
}