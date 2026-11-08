# Sistema de Gestión de Solicitudes Académicas

Trabajo Práctico Integrador (TPI) de **Metodología de Sistemas II** - Tecnicatura Universitaria en Programación a Distancia (UTN).

**Integrantes:** Lopez Angel, Lopez Leandro

## Descripción

Aplicación de consola en Java que permite gestionar las solicitudes de los estudiantes de una facultad
(certificados, equivalencias, reincorporaciones y otros trámites). Los datos que se usan son inventados.

El trabajo consiste en tomar un sistema funcional con deuda técnica (la versión inicial), diagnosticarla,
refactorizarla de a pasos con pruebas que aseguren que el comportamiento no cambia, y documentar el resultado.

## Funcionalidades

1. **Registrar una solicitud** con los datos del estudiante, el tipo, la descripción, la prioridad y la fecha.
2. **Cambiar el estado** de una solicitud (PENDIENTE, EN_PROCESO, RESUELTA, CERRADA), respetando las transiciones permitidas.
3. **Asignar un responsable** del área que corresponde al tipo de solicitud (máximo 3 solicitudes activas por responsable).
4. **Buscar solicitudes** por estado, por tipo o por legajo.
5. **Reporte** con totales por estado, detalle de cada solicitud (vencimiento y urgencia) y carga de cada responsable.

Los datos se guardan en un archivo `solicitudes.csv` que se crea al ejecutar el programa.

## Tecnologías

- Java 21
- Maven
- JUnit 5 (pruebas)
- Lizard (métricas de código)

## Cómo ejecutarlo

Desde IntelliJ IDEA: abrir la carpeta del repositorio, esperar a que Maven importe el proyecto y ejecutar la clase `solicitudes.Main`.

Para repetir el escenario de demostración desde una terminal de Windows (en la carpeta del proyecto, con el proyecto ya compilado):

```
del solicitudes.csv
cmd /c "java -cp target\classes solicitudes.Main < docs\escenario_demo_entrada.txt"
```

## Cómo correr las pruebas

Desde una terminal, en la carpeta del proyecto:

```
mvn test
```

Desde IntelliJ IDEA: clic derecho sobre `src/test/java` y **Run 'All Tests'**. Tienen que pasar las 44 pruebas.

Las pruebas de caja negra (`SolicitudesCajaNegraTest`) ejecutan el programa ya compilado, por eso hay que dejar que Maven o IntelliJ compilen antes de correrlas.

## Estructura del código

Todas las clases están en el paquete `solicitudes` (`src/main/java/solicitudes`):

| Clase | Qué hace |
|---|---|
| `Main` | Muestra el menú, pide los datos por teclado y llama al gestor y al reporte. |
| `GestorSolicitudes` | Reglas de negocio: registrar, cambiar de estado y asignar responsables. |
| `ReporteSolicitudes` | Arma el reporte y las búsquedas (solo lee los datos). |
| `RepositorioSolicitudes` | Interfaz con dos operaciones: `cargar` y `guardar`. |
| `RepositorioCsv` | Implementación que guarda las solicitudes en el archivo `solicitudes.csv`. |
| `Solicitud`, `Estudiante`, `Responsable` | Datos del sistema. `Solicitud` calcula su vencimiento y si es urgente; `Responsable` controla su carga. |
| `DatosSolicitud` | `record` que agrupa los datos para registrar una solicitud. |
| `Estado`, `Prioridad`, `TipoSolicitud` | Enumeraciones. `TipoSolicitud` sabe su área y sus días de plazo. |

Las pruebas están en `src/test/java/solicitudes`.

## Documentación (`docs/`)

| Archivo | Contenido |
|---|---|
| `diagnostico.md` | Etapas 2 a 4: calidad externa e interna, métricas, code smells, deuda técnica, SOLID y Clean Code, plan de refactoring. |
| `decisiones_tecnicas.md` | Decisiones de diseño, qué cambió en cada commit y qué se cambió a propósito en el comportamiento. |
| `code_review_checklist.md` | Etapa 7: checklist de revisión aplicado a la versión inicial y a la final, y hallazgos de la revisión. |
| `evidencias/metricas_comparativas.md` | Métricas antes y después, pruebas y cobertura. |
| `evidencias/lizard_*.txt` | Salidas completas de Lizard de la versión inicial y de la refactorizada. |
| `evidencias/ejecucion_v1.txt` | Salida del escenario de demostración de la versión inicial (la usa una prueba como referencia). |
| `escenario_demo_entrada.txt` | Respuestas que se le escriben al programa en el escenario de demostración. |
| `falla_paso1.txt`, `falla_paso2.txt` | Entradas que reproducen la falla del archivo cuando un nombre tiene `;` (versión inicial). |

## Estado del repositorio

Este repositorio sigue las etapas del TPI y cada hito está marcado con un tag:

| Tag | Qué marca |
|---|---|
| `estado-inicial` | Sistema base (versión inicial, con deuda técnica para diagnosticar) |
| `etapa-diagnostico` | Diagnóstico: calidad, métricas, code smells, SOLID y Clean Code |
| `etapa-refactoring` | Los 6 refactorings (Extract Method, Introduce Parameter Object, Replace Type Code with Enum, Move Method y dos Extract Class) |
| `etapa-pruebas` | Pruebas unitarias nuevas (44 pruebas en total) |
| `estado-refactorizado` | Código final, con los ajustes del code review |
| `etapa-docs` | Documentación final: code review, decisiones técnicas y evidencias |

Para ver el código de un hito: `git checkout <tag>`. Para volver: `git checkout main`.
