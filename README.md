# Control Prendario · API

API REST para la gestión administrativa de préstamos con garantía vehicular y de maquinaria agrícola de un almacén en Santa María, Huila. Proyecto del curso Computación en el Servidor, Universidad Surcolombiana.

El cliente web (Angular) está en el repositorio `controlprendario-frontend`.

## Tecnologías

Java 17 · Spring Boot 3.3.5 (Web, Data JPA, Security, Validation, AOP) · Hibernate 6.5 · MySQL 8 / TiDB Cloud · JWT (jjwt 0.12) · JasperReports 6.21 e iText 5 (PDF) · springdoc-openapi 2.3 (Swagger).

## Requisitos

- JDK 17 o superior
- Una base de datos MySQL 8 o TiDB Cloud (en desarrollo también sirve MariaDB)
- Maven 3.9 o superior (opcional: el proyecto incluye `mvnw`)

## Ambientes

| Perfil de Spring | API | Base de datos | Esquema |
|---|---|---|---|
| `local` (predeterminado) | `127.0.0.1:8080` | MySQL local en `localhost:3306`, esquema `controlprendario` | Usa el esquema existente (`none`). |
| `prod` (Oracle) | `127.0.0.1:8080`, detrás del proxy de la VM | TiDB Cloud. La URL y el usuario se configuran en el servicio. | Hibernate valida el esquema existente (`validate`). |

El frontend usa la misma ruta relativa `/api` en ambos ambientes. Para elegir el ambiente de la API, establece `SPRING_PROFILES_ACTIVE` en el proceso que la ejecuta.

### Variables

| Variable | Uso |
|---|---|
| `DB_URL`, `DB_USERNAME` | Requeridas en `prod`. |
| `LOCAL_DB_URL`, `LOCAL_DB_USERNAME` | Opcionales en `local`. Si se omiten, se usa MySQL en `localhost:3306` con el usuario `root`. |
| `DB_PASSWORD` | Contraseña de MySQL o TiDB, según el ambiente. |
| `JWT_SECRET` | Clave obligatoria en `prod`, de al menos 32 bytes. |
| `LOCAL_JWT_SECRET` | Opcional en `local`. Sírvete de una clave local de al menos 32 bytes si quieres conservar las sesiones entre reinicios. |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD`, `OPERATOR_USERNAME` / `OPERATOR_PASSWORD` | Configura el usuario y la contraseña de cada rol que vaya a iniciar sesión. |
| `JWT_EXPIRATION`, `SHOW_SQL`, `LOG_LEVEL_APP`, `LOG_LEVEL_SECURITY` | Opcionales. La vigencia predeterminada es de 8 horas y la salida de SQL está desactivada. |
| `APP_CORS_ALLOWED_ORIGINS` | Opcional, con orígenes exactos separados por comas. En `local` se admiten `localhost:4200` y `127.0.0.1:4200`. En `prod` el proxy permite usar el mismo dominio y la lista queda vacía. |
| `DDL_AUTO` | Permite elegir explícitamente la estrategia de Hibernate. El esquema de referencia está en `docs/10-modelo-relacional.sql`. |

### Ejecutar en local

Copia [`.env.example`](.env.example) a `.env` en la raíz del backend y completa `DB_PASSWORD` y las cuentas locales. Spring carga el archivo automáticamente con el perfil `local`. Usa la raíz del backend como directorio de trabajo en IntelliJ. Si necesitas otro directorio, `LOCAL_ENV_LOCATION` acepta una ubicación como `file:/ruta/al/.env`.

En una instalación local nueva, crea la base de datos y sus tablas con [el script del modelo relacional](docs/10-modelo-relacional.sql). Si ya tienes una base de datos, configura su conexión en `.env`.

Con MySQL escuchando en el puerto 3306 y las credenciales locales configuradas, ejecuta `./mvnw spring-boot:run` o `.\mvnw.cmd spring-boot:run` en PowerShell. La API queda en `http://localhost:8080` y Swagger en `/swagger-ui.html`. Spring genera una clave de sesión para el proceso local cuando `LOCAL_JWT_SECRET` está ausente. Si se reinicia la API, las sesiones firmadas con esa clave se invalidan.

### Ejecutar en Oracle

Para ejecutar Angular y la API en un solo contenedor, usa la [configuración de Docker Compose para Oracle](deploy/oracle/README.md).

En el servicio de Oracle, establece `SPRING_PROFILES_ACTIVE=prod` y configura sus credenciales como variables del proceso. El perfil `prod` no carga el `.env` local. Usa una URL JDBC de TiDB con TLS, por ejemplo:

```text
jdbc:mysql://<host>.tidbcloud.com:4000/controlprendario?sslMode=VERIFY_IDENTITY
```

Compila con `./mvnw clean package` y ejecuta `java -jar target/prendario-0.0.1-SNAPSHOT.jar`. Crea o actualiza el esquema de TiDB antes de arrancar si la validación detecta diferencias. El proxy de Oracle debe pasar el host, el protocolo y el puerto originales según el README del frontend.

## Autenticación y roles

1. `POST /api/auth/login` con `{"username": "...", "password": "..."}` devuelve un `accessToken`.
2. Las demás llamadas envían la cabecera `Authorization: Bearer <accessToken>`.

| Operación | Gerente (`ADMIN`) | Empleado (`OPERATOR`) |
|---|:---:|:---:|
| Clientes, vehículos, préstamos, garantías | ✔ | ✔ |
| Pagos, descuentos y correcciones de pagos | ✔ | ✔ |
| Contratos, recibos y resumen en PDF | ✔ | ✔ |
| Consultar movimientos y balance | ✔ | ✔ |
| Registrar entradas y salidas (`/api/movimientos`) | ✔ | ✘ (403) |
| Consultar auditoría (`/api/auditoria`) | ✔ | ✘ (403) |

## Endpoints

| Recurso | Ruta base | Operaciones |
|---|---|---|
| Autenticación | `/api/auth` | `POST /login` |
| Clientes | `/api/clientes` | crear, listar, `/buscar`, `/{id}`, `/{id}/historial`, actualizar, archivar |
| Vehículos | `/api/vehicles` | crear, listar, `/{id}`, actualizar, archivar |
| Préstamos | `/api/prestamos` | crear (vehículo o máquina), listar, `/{id}`, `/maquina/{id}`, `/buscar`, `/vencidos`, `/maquinas/vencidos`, actualizar, archivar |
| Préstamos de maquinaria | `/api/prestamos-maquinas` | crear, listar, `/{id}`, `/buscar`, `/vencidos`, archivar |
| Pagos | `/api/pagos` | registrar, listar, `/maquina`, `/descuento-interes/{idPrestamo}`, `/{idPago}/correccion` |
| Pagos de maquinaria | `/api/pagos-maquinas` | registrar, `/descuento-interes/{id}`, `/{idPago}/correccion` |
| Movimientos | `/api/movimientos` | registrar (solo gerente), listar, `/balance` |
| Garantías | `/api/garantias` | `/inventario` |
| Reportes PDF | `/api/reportes` | `/documento-empeno/{id}`, `/documento-maquina/{id}`, `/recibo-pago/{id}`, `/recibo-pago-maquina/{id}`, `/resumen-prestamos` |
| Auditoría | `/api/auditoria` | consulta paginada (solo gerente) |
| Préstamos (consulta) | `/api/public/prestamos` | `/buscar`, `/{id}` (requiere autenticación) |

Detalles de cada operación, parámetros y respuestas en Swagger.

### Reglas de comportamiento

- **Archivar en lugar de borrar.** `DELETE` en clientes, vehículos y préstamos marca el registro como inactivo (`activo = false`) y conserva su historial (RN-17).
- **Descuentos.** Un descuento al interés se guarda como un pago de intereses con `es_descuento = true`. Reduce el interés pendiente, pero no cuenta como dinero recibido en el balance (RN-14).
- **Auditoría.** Toda operación que crea, actualiza, archiva, corrige o aplica descuentos queda registrada con usuario, fecha, acción y resultado (RN-21).
- **Errores.** Los datos inválidos responden `400` con el detalle por campo; un documento duplicado responde `409`.
- **Resumen diario.** Todos los días a la 01:00 (hora de Bogotá) se recalculan saldos y estados de los préstamos.

## Respaldo y restauración

Los scripts usan `mysqldump` y `mysql`, y leen `DB_HOST`, `DB_PORT` (3306), `DB_NAME` (`controlprendario`), `DB_USERNAME`, `DB_PASSWORD` y `DB_SSL` (`true` para TiDB Cloud).

Generar un respaldo comprimido en `backups/` (carpeta excluida de Git):

```bash
./scripts/respaldo-db.sh
```

Restaurar un respaldo (reemplaza las tablas existentes):

```bash
./scripts/restaurar-db.sh backups/controlprendario_AAAAMMDD_HHMMSS.sql.gz --confirmar
```

### Migración de descuentos registrados antes de `es_descuento`

Los descuentos creados antes de este cambio se identifican por su observación. Para que el balance deje de contarlos como dinero recibido, ejecute una vez:

```sql
UPDATE pagos SET es_descuento = 1 WHERE observaciones = 'Descuento aplicado al interés pendiente';
UPDATE pagos_maquinas SET es_descuento = 1 WHERE observaciones = 'Descuento aplicado al interés';
UPDATE pagos SET es_descuento = 0 WHERE es_descuento IS NULL;
UPDATE pagos_maquinas SET es_descuento = 0 WHERE es_descuento IS NULL;
```

## Pruebas

```bash
./mvnw test
```

## Estructura

El código está organizado por funcionalidad (*vertical slicing*). Dentro de cada módulo, las clases se agrupan por capa (`controller`, `domain`, `dto`, `repository`, `service`); los módulos con necesidades adicionales también tienen paquetes como `mapper`, `config`, `aspect`, `scheduler` o `util`.

```
src/main/java/control/prendario/
├── client/       controller, domain, dto, repository, service
├── vehicle/      controller, domain, dto, mapper, repository, service
├── loan/         controller, domain, dto, repository, service, scheduler
├── payment/      controller, domain, dto, repository, service
├── movement/     controller, domain, repository, service
├── collateral/   controller, dto, service
├── report/       controller, dto, service, util
├── audit/        aspect, controller, domain, repository, service
├── security/     config, controller, dto, service
└── shared/       configuración transversal
scripts/          Respaldo y restauración de la base de datos
docs/             Diagramas UML, modelo relacional y uso de IA
```

Los nombres de clases, métodos y variables están en inglés. Los nombres de tablas y columnas, las propiedades JSON, las rutas y parámetros HTTP y los valores de los enums se conservan en español, porque forman parte del contrato con la base de datos y con el frontend.
