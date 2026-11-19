# Diagramas - Control Prendario

Diagramas UML del backend `controlprendario-api`, elaborados a partir del código de la rama `feature/yohan`.
Cada diagrama tiene su fuente editable en PlantUML (`fuentes/`) y su exportación en PNG (`png/`) y SVG (`svg/`).

| Ítem rúbrica | Diagrama | Fuente | Imagen |
|---|---|---|---|
| 7 | Diagrama de despliegue | [`07-despliegue.puml`](fuentes/07-despliegue.puml) | [PNG](png/07-despliegue.png) · [SVG](svg/07-despliegue.svg) |
| 8 | Diagrama de componentes | [`08-componentes.puml`](fuentes/08-componentes.puml) | [PNG](png/08-componentes.png) · [SVG](svg/08-componentes.svg) |
| 9 | Diagrama de clases (dominio) | [`09-clases-dominio.puml`](fuentes/09-clases-dominio.puml) | [PNG](png/09-clases-dominio.png) · [SVG](svg/09-clases-dominio.svg) |
| 10 | Modelo relacional | [`10-modelo-relacional.puml`](fuentes/10-modelo-relacional.puml) + [`../10-modelo-relacional.sql`](../10-modelo-relacional.sql) | [PNG](png/10-modelo-relacional.png) · [SVG](svg/10-modelo-relacional.svg) |
| 11 | Diagrama de clases (diseño por capas) | [`11-clases-diseno.puml`](fuentes/11-clases-diseno.puml) | [PNG](png/11-clases-diseno.png) · [SVG](svg/11-clases-diseno.svg) |

## Origen de cada diagrama

| Diagrama | Elementos del código que lo respaldan |
|---|---|
| Despliegue | `pom.xml` (Java 17, Spring Boot 3.3.5, MySQL Connector/J 8.3), `WebConfig.java` (orígenes CORS `localhost:4200` y `controlprendario.netlify.app`), `ResumenPrestamos.java` (cron 01:00 America/Bogota), informe de avance sección 15 (TiDB Cloud). |
| Componentes | Paquetes `controller`, `service`, `repository`, `scheduler` y `config`; dependencias tomadas de los campos inyectados (`@Autowired` / `final`) de cada clase. |
| Clases (dominio) | Entidades JPA del paquete `model` con sus atributos, enums y relaciones `@ManyToOne` / `@OneToMany`. |
| Modelo relacional | DDL generado por Hibernate 6.5.3 desde las entidades (`docs/10-modelo-relacional.sql`): tipos, `ENUM`, `UNIQUE` y llaves foráneas reales. |
| Clases (diseño) | Métodos públicos de controladores y servicios, interfaces `JpaRepository` y DTOs del paquete `DTO`. |

## Regenerar las imágenes

Requiere Java 17+ y Graphviz (`dot`). Desde `docs/diagramas`:

```bash
java -jar plantuml.jar -tpng -o ../png fuentes/*.puml
```

```bash
java -jar plantuml.jar -tsvg -o ../svg fuentes/*.puml
```

Se usó PlantUML 1.2024.7 y Graphviz 2.43.

Para regenerar el DDL del modelo relacional sin conectarse a una base de datos, desde la raíz del repositorio:

```bash
./mvnw -DskipTests package && java -jar target/prendario-0.0.1-SNAPSHOT.jar --spring.main.web-application-type=none --spring.jpa.hibernate.ddl-auto=none --spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect --spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create --spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=schema.sql --spring.jpa.properties.hibernate.hbm2ddl.delimiter=";" --spring.jpa.properties.hibernate.format_sql=true
```

El archivo `schema.sql` se escribe al iniciar el contexto; después la aplicación termina con error al no encontrar la base de datos, lo cual no afecta el script.
