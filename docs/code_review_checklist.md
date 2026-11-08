# Code review (Etapa 7)

Este documento tiene dos partes:

1. **Checklist de revisión**: las preguntas que nos hacemos al revisar el código, aplicadas a la versión inicial (`estado-inicial`) y a la versión refactorizada (`estado-refactorizado`).
2. **Hallazgos de la revisión del código final**: lo que encontramos al revisar la versión refactorizada y qué decidimos hacer con cada cosa.

Las métricas salen de Lizard (los resultados completos están en `docs/evidencias/`). La cobertura sale de IntelliJ ("Run with Coverage") sobre el tag `etapa-pruebas`.

Valoración: **Sí** (cumple), **Parcial** (cumple a medias) y **No** (no cumple).

---

## 1. Checklist de revisión

### A. Legibilidad y nombres

| # | Pregunta | v1 | v2 | Evidencia |
|---|---|---|---|---|
| A1 | ¿Los nombres de clases, métodos y variables dicen lo que son? | No | Sí | v1: `x`, `n`, `a`, `t`, `p`, `rr` (S13). v2: `puedeRecibirOtra`, `textoResponsable`, `contarPorEstado`. En bucles de pocas líneas todavía quedan `s` y `r`. |
| A2 | ¿Los comentarios explican qué hace el código sin repetirlo? | No | Sí | v1 tenía 5 comentarios en total (uno era `// nada` y otro código comentado). v2 tiene comentarios cortos en las clases y métodos que no se entienden solos. |
| A3 | ¿Se eliminó el código muerto y el código comentado? | No | Sí | Se borraron `x`, `imprimirDebug` y la línea comentada (R1 y R5). |
| A4 | ¿Los textos y números de negocio tienen nombre? | No | Sí | `MAXIMO_ACTIVAS`, días y área dentro de `TipoSolicitud`, `FORMATO_FECHA` (R3, R4 y ajuste del code review). |

### B. Métodos

| # | Pregunta | v1 | v2 | Evidencia |
|---|---|---|---|---|
| B1 | ¿Los métodos hacen una sola cosa y son cortos (hasta unas 30 líneas)? | No | Parcial | v1: 6 métodos de más de 40 líneas de código (el mayor, 90). v2: solo `registrarSolicitud` (53), `Main.main` (39) y `asignarResponsable` (32) pasan de 30. |
| B2 | ¿La complejidad ciclomática (CCN) es razonable (10 o menos)? | No | Parcial | v1: CCN máximo 20 y 5 métodos con 13 o más. v2: CCN máximo 12 (`registrarSolicitud`) y 11 (`Main.main`). Lizard ya no marca ninguna alerta. |
| B3 | ¿Los métodos tienen pocos parámetros (3 o menos)? | No | Sí | v1: `registrarSolicitud` con 7 parámetros. v2: máximo 2 (R2, `DatosSolicitud`). |
| B4 | ¿Se evita el anidamiento profundo? | No | Sí | `cambiarEstado` pasó de 7 niveles de `if` a 17 líneas y CCN 4 (R1). |
| B5 | ¿No hay código repetido? | No | Sí | Las 3 búsquedas copiadas son una sola (`buscar`); el formato de fecha está en una constante (R1, R6, ajuste del code review). |

### C. Diseño y SOLID

| # | Pregunta | v1 | v2 | Evidencia |
|---|---|---|---|---|
| C1 | **SRP**: ¿cada clase tiene una sola razón para cambiar? | No | Parcial | v1: `GestorSolicitudes` (350 líneas) hacía todo. v2: persistencia en `RepositorioCsv`, reporte y búsquedas en `ReporteSolicitudes`, menú en `Main`. El gestor todavía mezcla reglas con mensajes por pantalla (hallazgo H4). |
| C2 | **OCP**: ¿agregar un tipo o un estado se hace sin tocar varios `if`? | No | Sí | Un tipo nuevo es una línea más en `TipoSolicitud` (con su área y sus días). |
| C3 | **LSP**: ¿las implementaciones de una interfaz se pueden reemplazar entre sí? | No aplica | Sí | `RepositorioCsv` y `RepositorioEnMemoria` (de las pruebas) se usan con el mismo gestor sin cambiar nada. |
| C4 | **ISP**: ¿las interfaces son chicas? | No aplica | Sí | `RepositorioSolicitudes` tiene solo 2 operaciones: `cargar` y `guardar`. |
| C5 | **DIP**: ¿las reglas de negocio dependen de abstracciones? | No | Sí | El gestor recibe un `RepositorioSolicitudes` por constructor y ya no conoce archivos. |
| C6 | ¿Se evita el estado global estático? | No | Sí | `lista`, `responsables` y `contador` pasaron a ser campos privados de instancia (R5). |

### D. Manejo de errores y datos

| # | Pregunta | v1 | v2 | Evidencia |
|---|---|---|---|---|
| D1 | ¿Se evita tragar excepciones? | No | Sí | v1: `catch (...) { }` vacíos. v2: `RepositorioCsv` lanza `IllegalStateException` con el número de línea y `Main` avisa al usuario. |
| D2 | ¿Los datos guardados se pueden leer siempre, sea lo que sea que escriba el usuario? | No | Sí | v1: un `;` en el nombre rompía el archivo. v2: `;` y `%` se guardan codificados (prueba `elPuntoYComaYElPorcentajeSeConservan`). |
| D3 | ¿Los archivos se cierran siempre y se usa una codificación explícita? | No | Sí | `try-with-resources` y UTF-8 en `RepositorioCsv`. |
| D4 | ¿Se validan los datos de entrada? | Parcial | Parcial | Se validan nombre, legajo, email, tipo, descripción, prioridad y fecha. El email solo se controla que tenga `@` (hallazgo H10). |
| D5 | ¿Las reglas de negocio están separadas de la pantalla? | No | Parcial | `Main` solo pide datos y muestra el menú, pero el gestor todavía imprime sus mensajes de error (H4). |

### E. Pruebas

| # | Pregunta | v1 | v2 | Evidencia |
|---|---|---|---|---|
| E1 | ¿Hay pruebas automáticas que se puedan correr con un comando? | No | Sí | 5 pruebas de caja negra antes de refactorizar y 44 al final (`mvn test`). |
| E2 | ¿Se prueban los casos de error y no solo el camino feliz? | No | Sí | Email inválido, nombre vacío, límite de 3 solicitudes, transiciones no permitidas, archivo dañado. |
| E3 | ¿Las pruebas son independientes entre sí y no dejan archivos? | No aplica | Sí | Usan `@TempDir` o el repositorio en memoria. |
| E4 | ¿La cobertura es aceptable? | Sin medir | Sí | 72 % de las líneas y 97 % de los métodos. Gestor 93 %, reporte 100 %, repositorio CSV 96 %. |

### F. Modelo de datos

| # | Pregunta | v1 | v2 | Evidencia |
|---|---|---|---|---|
| F1 | ¿Los estados, tipos y prioridades son tipos propios (`enum`) y no textos? | No | Sí | `Estado`, `Prioridad` y `TipoSolicitud` (R3). |
| F2 | ¿Las reglas están en la clase que tiene los datos? | No | Parcial | `esUrgente` y `calcularVencimiento` están en `Solicitud`, y el límite de 3 está en `Responsable` (R4). `area` y `responsable` siguen siendo `String`. |
| F3 | ¿Los campos están encapsulados? | No | No | Los campos de `Solicitud`, `Estudiante` y `Responsable` siguen siendo públicos (H5). |

### G. Repositorio y entrega

| # | Pregunta | v1 | v2 | Evidencia |
|---|---|---|---|---|
| G1 | ¿Cada cambio va en un commit chico y con un mensaje claro? | Sí | Sí | Un commit por refactoring (R1 a R7), cada uno con las pruebas en verde. |
| G2 | ¿Los hitos están marcados con tags? | Sí | Sí | 6 tags (ver `README.md`). |
| G3 | ¿Los archivos generados no se suben? | Sí | Sí | `.gitignore` con `.idea/`, `target/` y `solicitudes.csv`. |
| G4 | ¿El README explica cómo ejecutar y probar? | Parcial | Sí | Se actualizó con las pruebas y la estructura del código. |

**Resultado:** de los 31 puntos revisados, la v2 cumple 24 del todo, 6 en parte y 1 no lo cumple (los campos públicos). Los 7 puntos pendientes son la deuda que explicamos en la parte 2.

---

## 2. Hallazgos de la revisión del código final

Revisamos las 12 clases del tag `etapa-pruebas` y, para cada hallazgo, decidimos si se corregía ahora o se dejaba como deuda conocida.

| # | Hallazgo | Gravedad | Decisión |
|---|---|---|---|
| H1 | `Main.main` seguía siendo un método largo (98 líneas de código, CCN 16) y era la única alerta de Lizard. El pedido de dos ids se repetía en dos opciones. | Media | **Corregido** (commit `37ccd25`): se separó en `mostrarMenu`, `registrar`, `cambiarEstado`, `asignarResponsable`, `buscar` y `leerId`. Ahora `main` tiene 39 líneas y CCN 11. |
| H2 | El formato `"dd/MM/yyyy"` estaba escrito en dos clases (`GestorSolicitudes` y `Solicitud`). | Baja | **Corregido** (commit `37ccd25`): constante `Solicitud.FORMATO_FECHA`. |
| H3 | `registrarSolicitud` sigue con 53 líneas y CCN 12: es una cadena de validaciones que imprimen cada error. | Media | **Deuda aceptada.** Se podría extraer un `validarDatos`, pero obliga a decidir cómo se avisan los errores (ver H4). Lo dejamos para no cambiar lo que ve el usuario. |
| H4 | Los mensajes de error se imprimen dentro de `GestorSolicitudes` (acoplamiento con la consola, S12). | Media | **Deuda aceptada.** La solución es que el gestor devuelva un resultado o lance excepciones propias y que `Main` imprima. Cambiaría la salida del programa y todas las pruebas de caja negra. |
| H5 | Los campos de `Solicitud`, `Estudiante` y `Responsable` son públicos (S14). | Media | **Deuda aceptada.** Encapsularlos toca casi todas las clases y no cambia el comportamiento. |
| H6 | `desdeTexto` está repetido en `Estado`, `Prioridad` y `TipoSolicitud` (8 líneas cada uno). | Baja | **Deuda aceptada.** Es repetición chica y quedó clara; se podría reemplazar por un método genérico. |
| H7 | `area` y `responsable` siguen siendo `String` (queda un resto de *primitive obsession*, S5). | Baja | **Deuda aceptada.** Sería una clase `Area` o usar directamente el objeto `Responsable`. |
| H8 | Si falla la escritura del archivo al guardar, `RepositorioCsv` lanza `UncheckedIOException` y `Main` no la captura, así que el programa se corta con el error. | Baja | **Deuda aceptada.** Antes el error se tragaba y se perdían los datos sin avisar; ahora al menos se ve. Falta mostrar un mensaje amigable. |
| H9 | Si la entrada se corta a mitad de una opción (solo pasa cuando se redirige un archivo, no con teclado), el programa termina con `NoSuchElementException`. | Baja | **Deuda aceptada.** Ya pasaba en la versión inicial y no afecta el uso normal. |
| H10 | La validación del email solo controla que tenga `@`. | Baja | **Fuera de alcance.** Es una regla de negocio que no se tocó para mantener el comportamiento. |
| H11 | La herramienta de cobertura marca `Main` en 0 %. | Informativa | **Se explica.** Las pruebas de caja negra ejecutan el programa como un proceso aparte, y la herramienta solo mide el proceso de las pruebas. `Main` sí está cubierto por la prueba del escenario completo. |

**Criterio para decidir:** corregimos lo que se podía arreglar sin cambiar lo que ve el usuario y con las pruebas en verde (H1 y H2). Lo que cambia el diseño de varias clases a la vez (H3, H4, H5) lo dejamos como deuda documentada: es la lista de trabajo para una próxima iteración.

---

*Este documento se redactó con ayuda de Claude (Anthropic). El grupo realizo el código, corrió las pruebas y verificó las métricas; el detalle del uso de IA está en el informe del TPI.*
