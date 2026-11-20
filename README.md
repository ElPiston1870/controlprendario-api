# Control Prendario · API

API REST para la gestión administrativa de préstamos con garantía vehicular y de maquinaria agrícola de un almacén en Santa María, Huila. Proyecto del curso Computación en el Servidor, Universidad Surcolombiana.

El cliente web (Angular) está en el repositorio `controlprendario-frontend`.

## Tecnologías

Java 17 · Spring Boot 3.3.5 (Web, Data JPA, Security, Validation, AOP) · Hibernate 6.5 · MySQL 8 / TiDB Cloud · JWT (jjwt 0.12) · JasperReports 6.21 e iText 5 (PDF) · springdoc-openapi 2.3 (Swagger).

## Requisitos

- JDK 17 o superior
- Una base de datos MySQL 8 o TiDB Cloud (en desarrollo también sirve MariaDB)
- Maven 3.9 o superior (opcional: el proyecto incluye `mvnw`)

## Configuración

Toda la configuración sensible se toma de variables de entorno. La aplicación no arranca sin `DB_PASSWORD` ni `JWT_SECRET`.

| Variable | Obligatoria | Descripción | Valor por defecto |
|---|---|---|---|
| `DB_URL` | No | URL JDBC de la base de datos | `jdbc:mysql://localhost:3306/controlprendario?createDatabaseIfNotExist=true` |
| `DB_USERNAME` | No | Usuario de la base de datos | `root` |
| `DB_PASSWORD` | Sí | Contraseña de la base de datos | — |
| `JWT_SECRET` | Sí | Clave para firmar los tokens (mínimo 32 caracteres) | — |
| `JWT_EXPIRATION` | No | Vigencia del token en milisegundos | `28800000` (8 horas) |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Sí* | Credenciales del gerente (rol `ADMIN`) | — |
| `OPERATOR_USERNAME` / `OPERATOR_PASSWORD` | Sí* | Credenciales del empleado (rol `OPERATOR`) | — |
| `DDL_AUTO` | No | Estrategia de esquema de Hibernate | `update` |
| `SHOW_SQL` | No | Mostrar las consultas SQL en el log | `false` |
| `LOG_LEVEL_APP` / `LOG_LEVEL_SECURITY` | No | Nivel de log de la aplicación y de Spring Security | `INFO` |

\* Hay que configurar al menos un usuario para poder iniciar sesión. Usuario y contraseña de un mismo rol van siempre juntos.

Para TiDB Cloud, la URL incluye TLS, por ejemplo:

```
jdbc:mysql://<host>.tidbcloud.com:4000/controlprendario?sslMode=VERIFY_IDENTITY
```

El esquema se crea y actualiza solo (`ddl-auto=update`) sin borrar datos. El DDL de referencia está en `docs/10-modelo-relacional.sql`.

## Ejecución

```bash
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080` y la documentación interactiva en `http://localhost:8080/swagger-ui.html`.

Para generar el `.jar`:

```bash
./mvnw clean package
```

```bash
java -jar target/prendario-0.0.1-SNAPSHOT.jar
```

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

Los orígenes permitidos por CORS están en `shared/WebConfig.java` (`http://localhost:4200` y `https://controlprendario.netlify.app`).

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
