# Métricas antes y después

Comparación entre la versión inicial (tag `estado-inicial`, commit `febfc22`) y la versión refactorizada (tag `estado-refactorizado`, commit `37ccd25`).

Las métricas de código se calcularon con **Lizard 1.24.3** sobre `src/main` de cada tag. Las salidas completas están en esta misma carpeta:

- `lizard_estado-inicial.txt`
- `lizard_estado-refactorizado.txt`

Para repetirlas: `pip install lizard` y, en la raíz del proyecto, `lizard src/main -l java`.

*NLOC* es la cantidad de líneas de código sin contar vacías ni comentarios; *CCN* es la complejidad ciclomática (cantidad de caminos del método).

## 1. Resumen general

| Métrica | Versión inicial | Versión refactorizada |
|---|---|---|
| Archivos de código (`src/main`) | 5 | 12 |
| Funciones | 15 | 47 |
| NLOC totales | 468 | 577 |
| NLOC promedio por función | 28,3 | 10,3 |
| CCN promedio por función | 7,5 | 2,7 |
| Función más larga (NLOC) | 90 (`Main.main`) | 53 (`registrarSolicitud`) |
| CCN más alto | 20 (`registrarSolicitud`) | 12 (`registrarSolicitud`) |
| Máximo de parámetros | 7 (`registrarSolicitud`) | 2 |
| Clase más grande (NLOC) | 350 (`GestorSolicitudes`) | 185 (`GestorSolicitudes`) |
| Funciones con alerta de Lizard | 1 | 0 |

El total de líneas **sube** (468 a 577) porque hay más clases, constructores, interfaces y métodos auxiliares. Lo que baja es el tamaño y la complejidad de cada pieza: el promedio por función pasa de 28 a 10 líneas.

## 2. Funciones principales

| Función (versión inicial) | NLOC / CCN antes | Dónde quedó | NLOC / CCN después | Refactoring |
|---|---|---|---|---|
| `Main.main` | 90 / 15 | `Main.main` y 6 métodos de `Main` | 39 / 11 (el mayor de los métodos nuevos: `buscar`, 19 / 4) | Code review (Extract Method) |
| `registrarSolicitud` | 61 / 20, 7 parámetros | `GestorSolicitudes.registrarSolicitud` | 53 / 12, 1 parámetro | R2, R3 |
| `cambiarEstado` | 56 / 15 | `GestorSolicitudes.cambiarEstado` + `transicionValida` | 17 / 4 + 16 / 6 | R1, R3 |
| `asignarResponsable` | 46 / 13 | `GestorSolicitudes.asignarResponsable` | 32 / 7 | R1, R4 |
| `reporte` | 48 / 13 | `ReporteSolicitudes.imprimirReporte` | 19 / 4 | R1, R6 |
| `buscarPorEstado`, `buscarPorTipo`, `buscarPorLegajo` | 12 / 4 cada una | `ReporteSolicitudes` (más un `buscar` común) | 3 / 1 cada una + 11 / 3 | R6 |
| `cargar` | 41 / 9 | `GestorSolicitudes.cargar` + `RepositorioCsv.cargar` y `leerLinea` | 15 / 6 + 18 / 4 + 27 / 4 | R5 |
| `guardar` | 13 / 3 | `RepositorioCsv.guardar` | 10 / 3 | R5 |
| `dias` | 11 / 4 | `TipoSolicitud.getDias` | 3 / 1 | R3 |
| `esUrgente` | 7 / 5 | `Solicitud.esUrgente` | 4 / 4 | R4 |
| `calcularVencimiento` | 5 / 1 | `Solicitud.calcularVencimiento` | 4 / 1 | R4 |
| `imprimirDebug` | 3 / 1 | (se borró: no se usaba) | — | R5 |

## 3. Pruebas

| | Antes de refactorizar | Después |
|---|---|---|
| Pruebas automáticas | 5 (caja negra) | 44 (6 de caja negra y 38 unitarias) |
| Resultado | 5 de 5 en verde | 44 de 44 en verde |
| Cobertura de líneas | sin medir | 72 % (233 de 321) |
| Cobertura de ramas | sin medir | 71 % (107 de 149) |
| Cobertura de métodos | sin medir | 97 % (47 de 48) |

Cobertura por clase (medida con IntelliJ sobre el tag `etapa-pruebas`):

| Clase | Líneas |
|---|---|
| `GestorSolicitudes` | 93 % |
| `ReporteSolicitudes` | 100 % |
| `RepositorioCsv` | 96 % |
| `Solicitud` | 100 % |
| `Responsable` | 100 % |
| `Estado` | 90 % |
| `Main` | 0 % (la cubren las pruebas de caja negra, que ejecutan el programa en otro proceso y la herramienta no las mide) |

Archivos de prueba (en `src/test/java/solicitudes`):

| Archivo | Pruebas | Qué prueba |
|---|---|---|
| `SolicitudesCajaNegraTest` | 6 | El programa completo: el escenario de demostración, las reglas principales, que los datos se conserven al reabrir y el archivo dañado. |
| `GestorSolicitudesTest` | 19 | Reglas del gestor con un repositorio en memoria: ids, validaciones, transiciones, límite de 3 y liberación de carga. |
| `SolicitudTest` | 7 | Vencimiento y urgencia. |
| `ReporteSolicitudesTest` | 6 | Búsquedas y reporte, con listas armadas a mano. |
| `RepositorioCsvTest` | 6 | Ida y vuelta de los datos, `;` y `%`, y archivos con errores. |
| `RepositorioEnMemoria` | — | Repositorio falso que usan las pruebas. |

**Prueba de que las pruebas detectan errores:** se rompió el código a propósito de 5 maneras (que cerrar una solicitud no libere la carga, un límite de 4 en lugar de 3, que `esUrgente` ignore las cerradas, que el repositorio no codifique el `;` y que el reporte cuente mal las pendientes) y en las 5 falló al menos una prueba.

## 4. Tags del repositorio

| Tag | Commit | Qué marca |
|---|---|---|
| `estado-inicial` | `febfc22` | Versión inicial |
| `etapa-diagnostico` | `f3de539` | Diagnóstico (calidad, smells, SOLID, Clean Code) |
| `etapa-refactoring` | `6b1bcac` | Los 6 refactorings (R1 a R6) |
| `etapa-pruebas` | `118da0f` | Pruebas unitarias nuevas (44 en total) |
| `estado-refactorizado` | `37ccd25` | Código final, con los ajustes del code review |
| `etapa-docs` | (commit de la documentación) | Code review, decisiones técnicas, evidencias y README final |
