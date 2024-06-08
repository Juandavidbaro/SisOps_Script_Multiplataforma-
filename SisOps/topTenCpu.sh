#!/bin/bash

rutaArchivo="cpuLog.txt"
intervalo=0.5
declare -A totalCpuPorProceso

while true; do
    procesos=$(ps -eo pid,etimes --no-headers)

    while read -r nombreProceso tiempoCpu; do
        if [[ -n "${totalCpuPorProceso[$nombreProceso]}" ]]; then
            totalCpuPorProceso[$nombreProceso]=$((totalCpuPorProceso[$nombreProceso] + tiempoCpu))
        else
            totalCpuPorProceso[$nombreProceso]=$tiempoCpu
        fi
    done <<< "$procesos"
    
    > $rutaArchivo
    
    for proceso in "${!totalCpuPorProceso[@]}"; do
        echo "$proceso ${totalCpuPorProceso[$proceso]}"
    done | sort -k2 -nr | head -n 10 | while read -r nombre totalCpu; do
        echo "$nombre: $totalCpu segundos" >> $rutaArchivo
    done
    
    sleep $intervalo
done
