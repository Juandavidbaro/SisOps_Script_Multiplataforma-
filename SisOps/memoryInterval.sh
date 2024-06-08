#!/bin/bash

if [ -z "$1" ]; then
    echo "Uso: $0 <PID>"
    exit 1
fi

pidSeleccionado=$1
intervalo=0.1
veces=$((10 * 30 * 1))
totalMemoriaPorProceso=0

usoMemoriaMin=999999999999
usoMemoriaMax=0
sumaMemoria=0
conteoMuestras=0

i=0
while [ $i -lt $veces ]; do
    proceso=$(ps -p $pidSeleccionado -o rss=)
    if [ -z "$proceso" ]; then
        echo "El proceso con PID $pidSeleccionado no se encuentra."
        break
    fi

    memoriaUsada=$((proceso / 1024))
    totalMemoriaPorProceso=$((totalMemoriaPorProceso + memoriaUsada))
    if [ $memoriaUsada -lt $usoMemoriaMin ]; then
        usoMemoriaMin=$memoriaUsada
    fi
    if [ $memoriaUsada -gt $usoMemoriaMax ]; then
        usoMemoriaMax=$memoriaUsada
    fi
    sumaMemoria=$((sumaMemoria + memoriaUsada))
    conteoMuestras=$((conteoMuestras + 1))

    i=$((i + 1))
    sleep $intervalo
done

if [ $conteoMuestras -gt 0 ]; then
    usoMemoriaPromedio=$(echo "scale=2; $sumaMemoria / $conteoMuestras" | bc)
else
    usoMemoriaPromedio=0
fi

echo "Uso de memoria para proceso con PID $pidSeleccionado"
echo "Uso mínimo de memoria: $usoMemoriaMin MB"
echo "Uso máximo de memoria: $usoMemoriaMax MB"
echo "Uso promedio de memoria: $usoMemoriaPromedio MB"
