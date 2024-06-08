param(
    [string]$ruta
)

if (-not $ruta) {
    Write-Host "Uso: .\script.ps1 -ruta <ruta>"
    exit 1
}

if (-not (Test-Path -Path $ruta -PathType Container)) {
    Write-Host "La ruta especificada no es válida."
    exit 1
}

$archivos_y_accesos = @{}
$archivos_frecuencia = @{}

$intervalo = 100
$veces = 10 * 30 * 1
$totalCpuPorProceso = 0

for ($i = 0; $i -lt $veces; $i++) {
    $archivos = Get-ChildItem -Path $ruta -File

    foreach ($archivo in $archivos) {
        if ($archivos_y_accesos.Contains($archivo.FullName)){
            if ($archivos_y_accesos[$archivo.FullName] -ne $archivo.LastAccessTime) {
            
                $archivos_frecuencia[$archivo.FullName] = $($archivos_frecuencia[$archivo.FullName] + 1)
            }
        } else {
            $archivos_frecuencia[$archivo.FullName] = 0
        }

        $archivos_y_accesos[$archivo.FullName] = $archivo.LastAccessTime
    }

    Start-Sleep -Milliseconds $intervalo
}

$archivos_ordenados = $archivos_frecuencia.GetEnumerator() | Sort-Object -Property Value -Descending | Select-Object -First 3

Write-Host "Los 3 archivos más accedidos en $ruta"
foreach ($archivo_info in $archivos_ordenados) {
    $archivo = $archivo_info.Key
    $frecuencia = $archivo_info.Value
    Write-Host "$archivo - Accesos: $frecuencia"
}