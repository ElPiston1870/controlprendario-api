#!/usr/bin/env bash
# Genera un respaldo comprimido de la base de datos de Control Prendario (RNF-10).
# Uso: ./scripts/respaldo-db.sh [directorio_destino]   (por defecto ./backups)
set -euo pipefail
source "$(dirname "$0")/_db-opciones.sh"

DESTINO="${1:-backups}"
mkdir -p "$DESTINO"
ARCHIVO="$DESTINO/controlprendario_$(date +%Y%m%d_%H%M%S).sql.gz"

OPCIONES=(--single-transaction --skip-lock-tables --no-tablespaces --default-character-set=utf8mb4)
# Opciones que solo existen en el cliente MySQL 8 (necesarias con TiDB Cloud)
if mysqldump --help 2>/dev/null | grep -q -- '--column-statistics'; then
  OPCIONES+=(--column-statistics=0)
fi
if mysqldump --help 2>/dev/null | grep -q -- '--set-gtid-purged'; then
  OPCIONES+=(--set-gtid-purged=OFF)
fi

mysqldump "${CONEXION[@]}" "${OPCIONES[@]}" "$DB_NAME" | gzip > "$ARCHIVO.tmp"
mv "$ARCHIVO.tmp" "$ARCHIVO"
echo "Respaldo generado: $ARCHIVO"
