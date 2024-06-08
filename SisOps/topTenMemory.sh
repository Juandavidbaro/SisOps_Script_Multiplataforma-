#!/bin/bash

rutaArchivo="memoryLog.txt"
intervalo=0.5
declare -A totalRAMPorProceso

while true; do
    procesos=$(ps -eo pid,rss --no-headers)
    
    while read -r nombreProceso ramUsada; do
        ramUsada=$((ramUsada / 1024))
        
        if [[ -n "${totalRAMPorProceso[$nombreProceso]}" ]]; then
            totalRAMPorProceso[$nombreProceso]=$((totalRAMPorProceso[$nombreProceso] + ramUsada))
        else
            totalRAMPorProceso[$nombreProceso]=$ramUsada
        fi
    done <<< "$procesos"
    
    > $rutaArchivo
    
    for proceso in "${!totalRAMPorProceso[@]}"; do
        echo "$proceso ${totalRAMPorProceso[$proceso]}"
    done | sort -k2 -nr | head -n 10 | while read -r nombre totalRAM; do
        echo "$nombre: $totalRAM MB" >> $rutaArchivo
    done
    
    sleep $intervalo
done
