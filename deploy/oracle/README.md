# Control Prendario en Oracle

La imagen reúne la API Spring Boot y los archivos compilados de Angular. El contenedor sirve ambos en el puerto `8080`. Angular consume `/api` bajo el mismo dominio. TiDB sigue siendo una base externa.

## Elegir la imagen

Ejecuta `uname -m` en la VM y elige el archivo correspondiente. Las instancias Oracle Ampere usan ARM. [Arquitecturas de Oracle Compute](https://docs.oracle.com/en-us/iaas/Content/Compute/References/arm.htm)

| Salida de `uname -m` | Archivo |
| --- | --- |
| `x86_64` | `control-prendario-oracle-amd64.tar.gz` |
| `aarch64` | `control-prendario-oracle-arm64.tar.gz` |

## Arrancar en la VM

Coloca el archivo de imagen y copia desde esta carpeta `compose.yaml` y `.env.oracle.example` en el mismo directorio. Usa Docker Compose 2.30 o posterior para que el archivo de variables conserve los caracteres especiales en las contraseñas. [Formato de variables de Docker Compose](https://docs.docker.com/reference/compose-file/services/#env_file)

```sh
docker load -i control-prendario-oracle-amd64.tar.gz  # usa el archivo de tu arquitectura
umask 077
cp .env.oracle.example .env.oracle
```

Completa `.env.oracle` con la URL JDBC y las credenciales de TiDB del ambiente que vas a desplegar, una clave aleatoria de al menos 32 bytes para `JWT_SECRET` y el usuario y la contraseña del gerente. El archivo de ejemplo configura TLS con verificación de identidad. El valor `DDL_AUTO=none` permite usar el esquema existente de Control Prendario. [TLS en TiDB Cloud](https://docs.pingcap.com/tidbcloud/tidb-cloud-tls-connect-to-dedicated/)

```sh
docker compose up -d --pull never
docker compose ps
docker compose logs -f app
```

Compose publica la aplicación en `127.0.0.1:8080` de la VM. Si ya tienes un proxy para el dominio, reenvía todas las rutas al contenedor y pasa las cabeceras del host y protocolo originales. Ejemplo dentro del bloque HTTPS de Nginx:

```nginx
location / {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

Puedes verificar el arranque desde la VM con `curl -f http://127.0.0.1:8080/index.html`. `APP_PORT` y `APP_BIND` son variables opcionales del shell o del archivo `.env` de Compose y controlan el puerto y la interfaz de la VM. Las credenciales se leen de `.env.oracle`.

## Reconstruir la imagen

Coloca los repositorios en una misma carpeta con los nombres `controlprendario-api` y `controlprendario-frontend`. Ejecuta lo siguiente desde la raíz del repositorio del backend. El valor predeterminado de la compilación es `linux/amd64`. Para Ampere, establece `CONTROL_PRENDARIO_PLATFORM=linux/arm64` en el entorno antes de compilar. En PowerShell se configura con `$env:CONTROL_PRENDARIO_PLATFORM = 'linux/arm64'`.

```sh
docker compose -f deploy/oracle/compose.yaml -f deploy/oracle/compose.build.yaml build
docker image save -o control-prendario-oracle.tar control-prendario:oracle
```

El archivo `.tar` se puede cargar directamente con `docker load -i`. Si publicas la imagen en tu registro, configura `CONTROL_PRENDARIO_IMAGE` con el nombre y la etiqueta publicados. Usa `docker compose pull` para descargarla en la VM antes de arrancar.
