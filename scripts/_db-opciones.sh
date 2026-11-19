#!/usr/bin/env bash
# Opciones de conexión comunes para respaldo-db.sh y restaurar-db.sh.
# Variables: DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, DB_SSL (true para TiDB Cloud).
set -euo pipefail

: "${DB_HOST:=localhost}"
: "${DB_PORT:=3306}"
: "${DB_NAME:=controlprendario}"
: "${DB_USERNAME:?Defina DB_USERNAME}"
: "${DB_PASSWORD:?Defina DB_PASSWORD}"
: "${DB_SSL:=false}"

# La contraseña viaja por variable de entorno, no por la línea de comandos.
export MYSQL_PWD="$DB_PASSWORD"

CONEXION=(--host="$DB_HOST" --port="$DB_PORT" --user="$DB_USERNAME")
if [[ "$DB_SSL" == "true" ]]; then
  if mysql --help 2>/dev/null | grep -q -- '--ssl-mode'; then
    CONEXION+=(--ssl-mode=VERIFY_IDENTITY)   # cliente MySQL 8
  else
    CONEXION+=(--ssl)                        # cliente MariaDB
  fi
fi
