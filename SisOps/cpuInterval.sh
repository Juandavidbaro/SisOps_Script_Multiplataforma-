#!/bin/bash

pidSeleccionado=$1
intervalo=0.1
veces=$((10 * 30 * 1))
totalCpuPorProceso=0

usoCpuMin=999999999999
usoCpuMax=0
sumaCpu=0
conteoMuestras=0

for ((i=0; i<veces; i++)); do
    if ! proceso=$(ps -p $pidSeleccionado -o %cpu=); then
        echo "El proceso con PID $pidSeleccionado no se encuentra."
        break
    fi

    cpuUsada=$(echo "$proceso" | awk '{print $1}')
    totalCpuPorProceso=$(echo "$totalCpuPorProceso + $cpuUsada" | bc)
    if (( $(echo "$cpuUsada < $usoCpuMin" | bc) )); then
        usoCpuMin=$cpuUsada
    fi
    if (( $(echo "$cpuUsada > $usoCpuMax" | bc) )); then
        usoCpuMax=$cpuUsada
    fi
    sumaCpu=$(echo "$sumaCpu + $cpuUsada" | bc)
    conteoMuestras=$(($conteoMuestras + 1))

    sleep $intervalo
done

if (( $conteoMuestras > 0 )); then
    usoCpuPromedio=$(echo "scale=2; $sumaCpu / $conteoMuestras" | bc)
else
    usoCpuPromedio=0
fi

echo "Uso de CPU para proceso con PID $pidSeleccionado"
echo "Uso minimo de CPU: $usoCpuMin %"
echo "Uso maximo de CPU: $usoCpuMax %"
echo "Uso promedio de CPU: $usoCpuPromedio %"
