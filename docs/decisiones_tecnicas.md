# Decisiones técnicas

Este documento explica **qué decidimos y por qué** durante el refactoring, qué cosas cambiaron a propósito en el comportamiento del programa y en qué commit quedó cada cambio.

## 1. Cómo trabajamos

- **Primero pruebas, después refactoring.** La versión inicial no se podía probar por partes (estado estático, pantalla mezclada con las reglas y un archivo con nombre fijo). Por eso antes de tocar el código escribimos pruebas de *caja negra*: ejecutan el programa completo como lo haría un usuario y comparan lo que imprime. Una de ellas repite el escenario de demostración y compara la salida con la que guardamos de la versión inicial (`docs/evidencias/ejecucion_v1.txt`).
- **Un refactoring por commit**, con las pruebas en verde antes de cada commit y un mensaje que dice la técnica usada.
- **Comprobar que no cambia el comportamiento.** Además de las pruebas, comparamos la salida del programa antes y después de cada refactoring con distintos escenarios (registros válidos e inválidos, cambios de estado, límite de 3 solicitudes, búsquedas, reportes, datos que se guardan y se vuelven a abrir). Las salidas fueron iguales, salvo los dos cambios intencionales de la sección 4.
- **Las pruebas viejas se cambian solo cuando cambia el comportamiento a propósito**, y en el mismo commit, para que se vea qué se decidió.

## 2. Decisiones de diseño

| # | Decisión | Alternativa que se descartó | Por qué |
|---|---|---|---|
| D1 | **Pruebas de caja negra** con `ProcessBuilder` y `@TempDir` antes de refactorizar. | Probar clases sueltas desde el principio. | En la versión inicial las clases no se podían probar sueltas: el estado era estático y escribían en un archivo fijo. |
| D2 | **`record DatosSolicitud`** para agrupar los 7 datos de una solicitud (R2). | Una clase con getters y setters, o un patrón *Builder*. | Es inmutable, tiene muy poco código y alcanza para pasar los datos juntos. |
| D3 | **Tres `enum`** (`Estado`, `Prioridad`, `TipoSolicitud`) en lugar de `String` (R3). | Seguir con textos y constantes. | El compilador controla los valores y desaparecen los `if/else` por tipo. |
| D4 | **`TipoSolicitud` lleva su área y sus días de plazo** (R3). | Un `switch` en el gestor. | Agregar un tipo nuevo es agregar una línea (OCP). |
| D5 | **`Estado` sabe a qué estados puede pasar** (`puedePasarA`). | Dejar la tabla de transiciones dentro de `cambiarEstado`. | La regla vive junto a los datos y `cambiarEstado` queda corto. |
| D6 | **Move Method**: `esUrgente` y `calcularVencimiento` pasan a `Solicitud`; el límite de 3 y la carga pasan a `Responsable` (R4). | Dejarlos en el gestor. | Usaban solo datos de esas clases (*Feature Envy*). El 3 ahora es la constante `MAXIMO_ACTIVAS`. |
| D7 | **Interfaz `RepositorioSolicitudes` con 2 operaciones** (`cargar` y `guardar`) (R5). | Un repositorio con alta, baja y modificación por solicitud. | El programa siempre lee y escribe todo el archivo; una interfaz más grande tendría métodos que nadie usa (ISP). |
| D8 | **El gestor recibe el repositorio por constructor** (R5). | Que el gestor cree el `RepositorioCsv`. | Con esto las pruebas usan un repositorio en memoria (DIP). |
| D9 | **Formato de archivo**: los `;` y `%` se guardan como `%3B` y `%25`. | Poner comillas a los textos, o cambiar el separador. | Los archivos que ya existían se siguen leyendo, porque solo cambia cómo se guardan los dos caracteres que antes rompían el archivo. |
| D10 | **`ReporteSolicitudes` recibe listas de solo lectura** (R6). | Pasarle el gestor entero, o listas modificables. | El reporte solo lee, y la clase se puede probar sin gestor. |
| D11 | **Una sola búsqueda con `Predicate`** y tres métodos de una línea (R6). | Tres métodos copiados. | Elimina la repetición (S6). |
| D12 | **El gestor sigue imprimiendo sus mensajes de error.** | Devolver resultados o lanzar excepciones propias. | Habría que cambiar todas las pruebas y la forma en que el usuario ve los errores. Queda como deuda (H4 del code review). |
| D13 | **Repositorio falso (`RepositorioEnMemoria`) escrito a mano** para las pruebas. | Usar Mockito. | No agrega una dependencia nueva y se lee fácil. |
| D14 | **`Main` dividido en métodos** y `FORMATO_FECHA` como constante (code review). | Dejar `Main.main` largo. | Era la única alerta que quedaba en Lizard. |

## 3. Qué se hizo en cada commit

| Commit | Técnica / etapa | Qué cambió | Smells |
|---|---|---|---|
| `febfc22` | Etapa 1 | Versión inicial (tag `estado-inicial`). | — |
| `f3de539` | Etapas 2 a 4 | `docs/diagnostico.md` (tag `etapa-diagnostico`). | S1 a S14 |
| `f7ab7e7` | Etapa 6 (antes) | 5 pruebas de caja negra sobre la versión inicial. | — |
| `6da4cd4` | **R1** Extract Method | `cambiarEstado`, `asignarResponsable` y `reporte` se parten en métodos chicos (`buscarSolicitud`, `buscarResponsable`, `liberarCarga`, `contarPorEstado`, `transicionValida`). | S2, S3, S6 |
| `b5ca551` | **R2** Introduce Parameter Object | `registrarSolicitud` pasa de 7 parámetros a 1 (`DatosSolicitud`). | S4 |
| `b577a1c` | **R3** Replace Type Code with Enum | `Estado`, `Prioridad` y `TipoSolicitud`. Se van los `if/else` por tipo y los textos mágicos. | S5, S7, S8 |
| `4e11e59` | **R4** Move Method | `esUrgente` y `calcularVencimiento` a `Solicitud`; `puedeRecibirOtra`, `sumarCarga` y `restarCarga` a `Responsable`. | S11, S14 |
| `4332050` | **R5** Extract Class | Persistencia en `RepositorioCsv` detrás de `RepositorioSolicitudes`; el gestor deja de ser estático; se borra código muerto. | S1, S9, S10, S13 |
| `6b1bcac` | **R6** Extract Class | `ReporteSolicitudes` (reporte y búsquedas) fuera del gestor (tag `etapa-refactoring`). | S1, S6 |
| `118da0f` | Etapa 6 (después) | 38 pruebas unitarias nuevas (tag `etapa-pruebas`). | — |
| `37ccd25` | Code review | `Main` dividido en métodos y constante `FORMATO_FECHA` (tag `estado-refactorizado`). | S2, S6 |

## 4. Cambios de comportamiento a propósito

Los dos cambios vienen del mismo problema de la versión inicial (S10): un `;` escrito en un nombre o en una descripción rompía el archivo.

1. **R3.** Con los `enum`, el reporte dejó de caerse con `DateTimeParseException` cuando había una línea rota. Pero la solicitud se perdía sin avisar al volver a abrir el programa (`Total: 0`). Por eso se cambió la prueba 5 en el mismo commit: antes comprobaba que el reporte se caía y pasó a comprobar la pérdida.
2. **R5.** Ahora el `;` y el `%` se guardan codificados y la solicitud **se conserva**, y si el archivo está dañado el programa **avisa** (`Error al leer los datos guardados: ...`) en lugar de perder datos. La prueba 5 volvió a cambiar: ahora exige `Total: 1` y que el nombre `Perez; Juan` se vea completo. Se agregó una prueba 6 para el archivo dañado.

## 5. Estado de cada code smell

| Smell | Estado | Cómo quedó |
|---|---|---|
| S1 God Class | Resuelto | `GestorSolicitudes` pasó de 350 a 185 líneas de código y se separaron `RepositorioCsv` y `ReporteSolicitudes` (R5 y R6). |
| S2 Long Method | Parcial | De 6 métodos de más de 40 líneas a uno solo: `registrarSolicitud`, con 53 (H3). |
| S3 Arrow code | Resuelto | `cambiarEstado` pasó de 56 líneas y CCN 15 a 17 líneas y CCN 4 (R1). |
| S4 Long Parameter List | Resuelto | 7 parámetros a 1 (R2). |
| S5 Primitive Obsession | Parcial | Estados, tipos y prioridades son `enum`. `area` y `responsable` siguen siendo `String` (H7). |
| S6 Código duplicado | Resuelto | R1, R6 y la constante `FORMATO_FECHA`. |
| S7 `if/else` por tipo | Resuelto | R3. |
| S8 Números y textos mágicos | Resuelto | `MAXIMO_ACTIVAS`, días y área en `TipoSolicitud`, `FORMATO_FECHA`. |
| S9 Estado global estático | Resuelto | R5. |
| S10 Excepciones tragadas | Resuelto | R5 (`RepositorioCsv` avisa y `Main` muestra el error). |
| S11 Feature Envy | Resuelto | R4. |
| S12 Lógica pegada a la consola | Parcial | `Main` solo maneja el menú y el reporte tiene su clase, pero el gestor todavía imprime sus errores (H4). |
| S13 Código muerto y nombres | Resuelto | Se borraron `x` e `imprimirDebug`; los nombres mejoraron (quedan `s` y `r` en bucles cortos). |
| S14 Modelo anémico | Parcial | Las clases del modelo ya tienen comportamiento, pero los campos siguen siendo públicos (H5). |

**Resumen:** 10 resueltos y 4 parciales de 14. Las métricas antes y después están en `docs/evidencias/metricas_comparativas.md`.

## 6. Qué haríamos en una próxima iteración

1. Encapsular los campos del modelo (H5) y reemplazar `area` y `responsable` por tipos propios (H7).
2. Hacer que el gestor devuelva un resultado en lugar de imprimir (H4) y, con eso, dividir `registrarSolicitud` en validaciones chicas (H3).
3. Mostrar un mensaje claro cuando falla la escritura del archivo (H8).

---

*Este documento se redactó con ayuda de Claude (Anthropic). El grupo realizo el código, corrió las pruebas y verificó las métricas con ayuda de la IA; el detalle del uso de IA está en el informe del TPI.*
