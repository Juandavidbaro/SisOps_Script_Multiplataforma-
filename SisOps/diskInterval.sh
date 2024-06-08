#!/bin/bash

# Usage function
usage() {
    echo "Uso: $0 -ruta <ruta>"
    exit 1
}

# Check for the ruta argument
while getopts ":r:" opt; do
    case ${opt} in
        r )
            ruta=$OPTARG
            ;;
        \? )
            usage
            ;;
    esac
done

if [ -z "$ruta" ]; then
    usage
fi

if [ ! -d "$ruta" ]; then
    echo "La ruta especificada no es válida."
    exit 1
fi

declare -A archivos_y_accesos
declare -A archivos_frecuencia

intervalo=0.1  # 100 milliseconds
veces=$((10 * 30 * 1))  # Adjust as necessary

for ((i = 0; i < veces; i++)); do
    for archivo in "$ruta"/*; do
        if [ -f "$archivo" ]; then
            last_access=$(stat -c %X "$archivo")
            if [[ -v archivos_y_accesos["$archivo"] ]]; then
                if [ "${archivos_y_accesos["$archivo"]}" -ne "$last_access" ]; then
                    archivos_frecuencia["$archivo"]=$((archivos_frecuencia["$archivo"] + 1))
                fi
            else
                archivos_frecuencia["$archivo"]=0
            fi
            archivos_y_accesos["$archivo"]=$last_access
        fi
    done
    sleep $intervalo
done

# Sort and get the top 3 accessed files
sorted_archivos=$(for archivo in "${!archivos_frecuencia[@]}"; do
    echo "$archivo ${archivos_frecuencia["$archivo"]}"
done | sort -k2 -nr | head -n 3)

echo "Los 3 archivos más accedidos en $ruta"
echo "$sorted_archivos" | while read -r archivo accesos; do
    echo "$archivo - Accesos: $accesos"
done
