# 13. Uso de inteligencia artificial en el desarrollo

Este documento declara la herramienta de inteligencia artificial utilizada durante el desarrollo y la documentación de Control Prendario, las instrucciones (prompts) empleadas, los mecanismos de control aplicados y la relación con las reglas de negocio del proyecto, conforme al ítem 13 de la rúbrica de documentación.

> **Aclaración.** La sección 14 del informe de avance indica que "no se atribuye uso de inteligencia artificial al MVP". Esa afirmación se refiere al **producto**: la aplicación no incorpora funciones de IA para el usuario final. Este documento trata del uso de IA como **herramienta de apoyo en el proceso de desarrollo**, que es lo que pide el ítem 13.

## 13.1 Alcance del uso de IA

| Aspecto | Declaración |
|---|---|
| Herramientas | **Claude Code** (Anthropic), asistente de programación ejecutado en un entorno de nube aislado con acceso al repositorio `controlprendario-api`, y **GitHub Copilot** para la ampliación de las pruebas unitarias descrita en la sección 13.9. |
| Herramientas de IA utilizadas | **Claude Code** para las actividades descritas en las secciones 13.2 a 13.8 y **GitHub Copilot** para apoyar la ampliación de las pruebas unitarias (sección 13.9). |
| Actividades apoyadas | 1. **Elaboración de diagramas** de la documentación técnica (ítems 7, 8, 9, 10 y 11 de la rúbrica). 2. **Redacción de historias de usuario detalladas, pila del producto y lista de tareas** (ítems 3, 4 y 5) a partir del informe de avance y del código. 3. **Revisión del código y corrección de pendientes del backend** (sección 13.8). 4. **Ampliación de las pruebas unitarias** (sección 13.9). |
| Actividades **no** realizadas con IA | Análisis del problema, catálogo de requisitos, historias de usuario base (HU-01 a HU-10, sección 9 del informe), priorización MoSCoW, reglas de negocio, código fuente base de la aplicación (backend y frontend), funcionalidades RF-01 a RF-24 (incluidas seguridad JWT, auditoría, archivo lógico, inventario de garantías y resumen PDF), cálculo de intereses e informe de avance. Todo esto fue elaborado por el equipo. |
| Responsable de la interacción | Yohan Bustos (Desarrollador). |
| Rama de trabajo | `feature/yohan` |

## 13.2 Artefactos producidos con apoyo de Claude Code

| Ítem | Artefacto | Archivos |
|---|---|---|
| 7 | Diagrama de despliegue | `docs/diagramas/fuentes/07-despliegue.puml` |
| 8 | Diagrama de componentes | `docs/diagramas/fuentes/08-componentes.puml` |
| 9 | Diagrama de clases (dominio) | `docs/diagramas/fuentes/09-clases-dominio.puml` |
| 10 | Modelo relacional | `docs/diagramas/fuentes/10-modelo-relacional.puml`, `docs/10-modelo-relacional.sql` |
| 11 | Diagrama de clases (diseño por capas) | `docs/diagramas/fuentes/11-clases-diseno.puml` |
| — | Índice de diagramas | `docs/diagramas/README.md` |
| 3 | Historias de usuario con criterios de aceptación | `Historias_Usuario_Control_Prendario.docx` (fuera del repositorio) |
| 4, 5 | Pila del producto y lista de tareas | `Pila_Producto_y_Lista_Tareas_Control_Prendario.xlsx` (fuera del repositorio) |

Las imágenes `png/` y `svg/` no fueron "dibujadas" por la IA: se generaron de forma determinista con PlantUML 1.2024.7 y Graphviz 2.43 a partir de las fuentes `.puml`.

## 13.3 Instrucciones (prompts) empleadas

Se transcriben las instrucciones enviadas a Claude Code, en el orden en que se dieron.

**Prompt 1: análisis de faltantes**

> Tengo este documento que viene alineado a la hoja 2 del excel relacionado al proyecto, aun nos falta agregar algunas cosas al repositorio y en la primera hoja nos piden varias cosas que no tenemos. Quiero que me ayudes a analizar en cuanto al excel que hace falta primero que todo.

Archivos adjuntos: el informe de avance en PDF (`01_Informe_Avance_Control_Prendario`) y la hoja "Documentación" de la rúbrica en CSV.

Resultado: una tabla con el estado de cada entregable (completo, parcial o faltante) y la propuesta de empezar por los diagramas y por este documento. El análisis lo revisó el equipo y no se versionó como entregable.

**Prompt 2: elaboración de diagramas**

> Si, de hecho me parece bien. Para el apartado 13 atribuye unicamente a claude code para la creacion de diagramas. Vamos con eso

Resultado: los diagramas de la sección 13.2 y este documento.

**Prompt 3: planificación de los siguientes entregables**

> Que puntos faltarian

Resultado: lista de entregables pendientes de la rúbrica y propuesta de orden de trabajo.

**Prompt 4: historias de usuario, pila del producto y lista de tareas**

> Vamos entonces con 4,5 y 3 pero entiendo que eso son documentos que no se suben al repositorio

Opciones elegidas por el equipo ante las preguntas de la herramienta: formato Excel (pila y tareas) + Word (historias); actualizar este documento; sin sprints ni estimaciones, solo prioridad MoSCoW y estado.

Resultado: los dos documentos de la sección 13.2 (ítems 3, 4 y 5).

## 13.4 Procedimiento seguido por la herramienta

1. **Lectura del código fuente.** Se leyeron las entidades (`model/`), controladores, servicios, repositorios, DTOs, el `scheduler`, la configuración (`WebConfig`, `SwaggerConfig`), `pom.xml` y `application.properties`.
2. **Escritura de fuentes PlantUML.** Cada elemento del diagrama corresponde a una clase, atributo, método, dependencia inyectada o configuración que existe en el código.
3. **Modelo relacional a partir del ORM.** El DDL de `docs/10-modelo-relacional.sql` **no fue redactado por la IA**: se generó con la exportación de esquema de Hibernate 6.5.3 sobre las entidades JPA, sin conexión a base de datos. El diagrama relacional se ajustó para coincidir con ese DDL (por ejemplo, columnas `ENUM` nativas y `DECIMAL(38,2)`).
4. **Renderizado y revisión visual.** Se exportaron PNG y SVG y se inspeccionó cada imagen para corregir problemas de legibilidad (por ejemplo, se reubicaron las interfaces del diagrama de componentes).
5. **Versionado.** Los cambios se confirmaron en la rama `feature/yohan`, sin modificar el código de la aplicación.
6. **Historias, pila y tareas.** Las historias HU-01 a HU-10, sus requisitos y prioridades se tomaron sin cambios del informe de avance. La herramienta redactó los criterios de aceptación a partir de las reglas de negocio y de los casos numéricos CP-09 a CP-12 y CP-15. El estado de cada historia y tarea se verificó leyendo el código y el historial de commits; cuando el código no cumple un criterio, se dejó marcado como observación en lugar de darlo por cumplido. Las pantallas del frontend no se revisaron (repositorio no disponible) y se marcaron según el informe o como «Por verificar».

## 13.5 Mecanismos de control

| Mecanismo | Descripción |
|---|---|
| Trazabilidad al código | Todo elemento de los diagramas tiene una contraparte verificable en el repositorio; `docs/diagramas/README.md` indica de qué archivos proviene cada diagrama. |
| Generación determinista | El DDL lo produce Hibernate y las imágenes PlantUML/Graphviz; la IA no inventa tipos, restricciones ni geometría. |
| Fuentes editables | Los `.puml` son texto plano, revisable en un *diff* de Git y modificable por el equipo sin depender de la IA. |
| Revisión humana | El equipo revisa cada diagrama contra el código antes de integrarlo a la rama principal, y valida criterios de aceptación, estados y responsables propuestos de las historias y tareas (ver lista 13.7). |
| Aislamiento de la rama | El trabajo se hizo en `feature/yohan`; la IA no tiene permiso para integrar cambios en otras ramas ni para crear *pull requests* sin solicitud explícita. |
| Cambios al software acotados y probados | Los cambios descritos en la sección 13.8 corrigieron pendientes identificados en la revisión. Posteriormente, GitHub Copilot amplió las pruebas unitarias, como se documenta en la sección 13.9. |
| Protección de datos | No se usaron datos reales de clientes. Las credenciales de `application.properties` no se copiaron a la documentación. |
| Entorno aislado | Claude Code se ejecutó en un contenedor en la nube con acceso solo al repositorio autorizado; los comandos quedaron sujetos al modo de permisos de la sesión. |

## 13.6 Reglas de negocio, agentes y skills

| Componente | Uso en este proyecto |
|---|---|
| **Reglas de negocio** | La IA **no definió ni modificó** reglas de negocio; en la sección 13.8 solo implementó en el código reglas ya definidas por el equipo (RN-01, RN-02, RN-14, RN-16). Las reglas RN-01 a RN-22 son del equipo (informe, sección 8). Los diagramas solo las reflejan donde ya están en el código: unicidad de documento (RN-01) y placa, tasa por defecto 5 % y plazo de 3 meses en `@PrePersist` (RN-07, RN-08), estados del préstamo (RN-22), trazabilidad de correcciones de pagos (RF-19 / RN-17) y movimientos independientes para el balance (RN-16). |
| **Agentes** | Claude Code se utilizó en la sesión principal para las actividades hasta la sección 13.8; GitHub Copilot se utilizó en la sesión principal para la ampliación de pruebas de la sección 13.9. No se delegaron tareas a subagentes ni se configuraron agentes personalizados. |
| **Skills** | No se invocaron *skills* de Claude Code ni se agregaron al repositorio. |
| **Reglas para la IA (CLAUDE.md u otras)** | El repositorio no contiene archivos de instrucciones para la IA. Las restricciones iniciales se dieron en los prompts de Claude Code: limitar su trabajo a los diagramas y trabajar en la rama `feature/yohan`. La ampliación de pruebas con GitHub Copilot se documenta en la sección 13.9. |
| **Herramientas externas** | PlantUML 1.2024.7 (descargado de Maven Central), Graphviz 2.43 y Maven/Hibernate del propio proyecto. |

## 13.7 Lista de verificación del equipo

Antes de integrar los diagramas a la rama principal, el equipo confirma:

- [ ] Las entidades y atributos del diagrama de clases coinciden con `src/main/java/control/prendario/model`.
- [ ] Las multiplicidades reflejan las reglas del negocio (un cliente con varios préstamos; cada pago con exactamente un préstamo).
- [ ] El modelo relacional coincide con la base de datos desplegada en TiDB Cloud.
- [ ] El diagrama de despliegue refleja el hospedaje real del frontend y del backend en la demostración.
- [ ] Las dependencias del diagrama de componentes corresponden a las inyecciones de cada clase.
- [ ] Ningún diagrama contiene credenciales, URL privadas ni datos personales reales.
- [ ] Los criterios de aceptación de HU-01 a HU-10 reflejan lo acordado con el gerente.
- [ ] Los estados de la pila y de la lista de tareas son correctos, incluidas las pantallas del frontend.
- [ ] Los usuarios de Git se reemplazaron por los nombres de los integrantes y se confirmaron los responsables propuestos.

Revisado por: ____________________   Fecha: ____________

## 13.8 Revisión y corrección del backend

**Prompt 5: revisión de los cambios del equipo**

> Bueno hice unos cambios gigantes al repositorio, en teoria completando las tareas que faltaban, creo que me quedaria faltando unicamente test unitarios, pero eso lo puedo hacer luego. Por favor revisa y verifica teniendo en cuenta el doc y el excel que me diste, creeria que ya esta todo

Resultado: revisión del commit `24bb93c` (RF-20 a RF-24, elaborado por el equipo). La herramienta compiló el proyecto, ejecutó las pruebas y detectó, con pruebas temporales, que el *preflight* CORS era rechazado, que el rol empleado no podía gestionar clientes ni préstamos y que la consulta pública exponía datos personales.

**Prompt 6: corrección de pendientes**

> Quiero pedirte por favor que arregles el resto de cosas faltantes, si es posible, por ahora no pienses en frontend ni me des mas documentancion lo ideal es que el repo este bien para ya poder documentar. Añade lo que dices al repo

Cambios realizados por la herramienta:

| Cambio | Archivos | Requisito / regla |
|---|---|---|
| CORS habilitado en Spring Security; `/error` accesible para que los rechazos respondan 403 en lugar de 401 | `config/SecurityConfig.java` | RNF-07 |
| Permisos por rol: el empleado gestiona clientes, préstamos, pagos, descuentos, correcciones y reportes; solo el gerente registra movimientos y consulta la auditoría | `config/SecurityConfig.java` | RF-21, RN-16 |
| `/api/public/prestamos` deja de ser público | `config/SecurityConfig.java` | RNF-05 |
| Descuentos marcados con `es_descuento` y excluidos del dinero recibido en el balance | `model/Pago.java`, `model/PagoMaquina.java`, repositorios y servicios de pago | RF-17, RN-14 |
| Validación de importe, tipo y préstamo al registrar pagos | `service/PagoService.java`, `service/PagoMaquinaService.java` | HU-04 |
| Validación de datos obligatorios del cliente y respuesta 409 para documento duplicado | `model/Cliente.java`, `controller/ClienteController.java`, `service/DocumentoDuplicadoException.java` | RN-01, RN-02 |
| Manejo global de errores (400, 404, 409) | `config/GlobalExceptionHandler.java` | RNF-01 |
| Corrección de error preexistente: el balance fallaba cuando no había pagos de intereses de maquinaria | repositorios de pago | RF-14 |
| Configuración por variables de entorno, token de 8 horas y logs sin datos personales | `application.properties` | RNF-05 |
| Scripts de respaldo y restauración | `scripts/` | RNF-10 |
| Guía de instalación y operación | `README.md` | — |
| Pruebas automáticas de los cambios (13 pruebas nuevas) | `SecurityRulesTest`, `PagoValidacionDescuentoTest` | — |
| Actualización de diagramas y DDL con el esquema nuevo | `docs/` | — |

Verificación: las 24 pruebas del proyecto pasan (`./mvnw test`). Además, la API se ejecutó contra una base MariaDB local y se recorrió el flujo de login, roles, validaciones, préstamo, pago, descuento, balance, auditoría y resumen PDF. Los scripts de respaldo y restauración se probaron con un respaldo, el borrado de la base y su restauración.

- [ ] El equipo revisó los cambios de código de esta sección antes de integrarlos a la rama principal.

## 13.9 Ampliación de las pruebas unitarias

GitHub Copilot se encargó de la gran mayoría de las pruebas unitarias añadidas para ampliar la cobertura del backend. La suite cubre de forma general servicios y reglas de negocio, cálculos de intereses, reportes PDF, auditoría, conversión de datos y controladores. También se configuró JaCoCo para medir la cobertura y exigir un mínimo global del 80 % de líneas durante la fase `verify`.

En la última ejecución documentada, `./mvnw clean verify` completó con **81 pruebas, sin fallos ni errores**, y JaCoCo reportó **82,01 % de cobertura de líneas**. La cobertura de ramas fue de 63,26 %. Estos resultados reflejan la ejecución automatizada de la suite y no sustituyen la revisión del equipo sobre los casos y las reglas de negocio.

- [ ] El equipo revisó las pruebas añadidas y confirmó que sus casos representan el comportamiento esperado.
