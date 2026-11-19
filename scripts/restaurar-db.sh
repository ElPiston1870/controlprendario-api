#!/usr/bin/env bash
# Restaura un respaldo generado por respaldo-db.sh. REEMPLAZA las tablas existentes.
# Uso: ./scripts/restaurar-db.sh <archivo.sql.gz> --confirmar
set -euo pipefail

if [[ $# -lt 2 || "$2" != "--confirmar" ]]; then
  echo "Uso: $0 <archivo.sql.gz> --confirmar" >&2
  echo "La restauración reemplaza las tablas de la base \${DB_NAME:-controlprendario}." >&2
  exit 1
fi
ARCHIVO="$1"
[[ -f "$ARCHIVO" ]] || { echo "No existe el archivo $ARCHIVO" >&2; exit 1; }

source "$(dirname "$0")/_db-opciones.sh"

mysql "${CONEXION[@]}" -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4"
gunzip -c "$ARCHIVO" | mysql "${CONEXION[@]}" --default-character-set=utf8mb4 "$DB_NAME"
echo "Base $DB_NAME restaurada desde $ARCHIVO"
