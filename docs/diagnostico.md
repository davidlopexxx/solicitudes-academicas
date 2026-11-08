# Diagnóstico del sistema (versión inicial)

Sistema: **Gestión de Solicitudes Académicas** (Java, consola).
Código analizado: tag `estado-inicial` (commit `febfc22`).
Integrantes: Lopez Angel, Lopez Leandro.

Este documento junta las etapas 2, 3 y 4 del TPI: calidad externa e interna, code smells con deuda técnica, y SOLID / Clean Code.

---

## 1. Calidad externa vs. calidad interna

- **Calidad externa:** lo que nota quien usa el sistema. Si hace lo que tiene que hacer, si se rompe, si los mensajes se entienden.
- **Calidad interna:** cómo está hecho el código por dentro. Si se lee fácil, si se puede cambiar sin romper otra cosa, si se puede probar.

### 1.1 Calidad externa de la versión inicial

| Atributo | Cómo está en la v1 | Evidencia | Valoración |
|---|---|---|---|
| Funcionalidad | Cumple las 5 funciones: registrar, cambiar estado, asignar responsable, buscar y reporte. La salida del escenario de demo coincide con la esperada (106 líneas). | `docs/evidencias/ejecucion_v1.txt` | Buena |
| Usabilidad | Menú numerado y mensajes de error claros (por ejemplo "no se puede pasar a EN_PROCESO sin responsable asignado"). Pero el tipo y la prioridad se escriben a mano y, si te equivocás en un dato, hay que cargar todo de nuevo. | `docs/evidencias/ejecucion_v1.txt` | Aceptable |
| Fiabilidad | **Mala.** Si el nombre tiene un `;` (por ejemplo `Perez; Juan`) la solicitud se guarda mal en el archivo y después el reporte se cae con una excepción. | `docs/falla_paso1.txt` y `docs/falla_paso2.txt` (datos de entrada que reproducen la falla) | Mala |
| Integridad de los datos | Validación mínima: el mail solo se controla que tenga `@`. Una descripción con `;` se cambia por `,` sin avisar (`GestorSolicitudes.java`, línea 99). | Código | Débil |
| Eficiencia | No es un problema con pocos datos (búsquedas recorriendo la lista). | Código | No relevante |

### 1.2 Calidad interna de la versión inicial

| Atributo | Cómo está en la v1 | Evidencia | Valoración |
|---|---|---|---|
| Legibilidad | Nombres poco claros: `x`, `n`, `a`, `t`, `p`, `e`, `r`, `c`, `rr`. | Líneas 18, 20, 296, 324 a 327, 366 de `GestorSolicitudes.java` | Mala |
| Cohesión y modularidad | Una sola clase (`GestorSolicitudes`, 350 líneas de código, 14 métodos) hace casi todo. | Métricas de la sección 2 | Mala |
| Acoplamiento | Todo es `static`, hay 38 `System.out.println` adentro de la lógica y el nombre del archivo está fijo en el código. | Líneas 14 a 18 y todo el archivo | Mala |
| Complejidad | Hay métodos con complejidad ciclomática 20 y 15, y anidamiento de hasta 7 niveles. | Métricas de la sección 2 | Mala |
| Duplicación | Tres búsquedas casi iguales y cuatro bucles de conteo iguales. | Líneas 256 a 293 y 324 a 347 | Mala |
| Facilidad de prueba | No hay ninguna prueba automática y las reglas de negocio no se pueden probar sin pasar por la consola y el archivo. | Carpeta `src/test` vacía | Mala |

### 1.3 Cómo se relacionan

La falla de `Perez; Juan` se ve desde afuera (calidad externa), pero la causa está adentro (calidad interna):

1. El archivo se guarda con `;` como separador y no se escapa el texto (`guardar`, línea 98).
2. Las excepciones se tragan: `catch (IOException e) { // nada }` en `guardar` y `catch (Exception e) {}` en `cargar` (líneas 103 y 146).
3. La carga lee los campos por posición, así que un `;` de más corre todos los datos y la fecha termina valiendo `PENDIENTE`.
4. La validación de datos y la persistencia están mezcladas en la misma clase, entonces nadie las controla por separado.

Conclusión: los problemas internos de hoy son los errores visibles de mañana.

---

## 2. Métricas del código (versión inicial)

Herramienta: **Lizard**, ejecutada por el grupo de trabajo (con asistencia de Gemini) sobre el código del tag `estado-inicial`.
**NLOC** = líneas de código sin contar vacías ni comentarios. **CCN** = complejidad ciclomática (cantidad de caminos distintos dentro del método; arriba de 10 ya se considera alta).

| Método | Líneas (archivo) | NLOC | CCN | Parámetros |
|---|---|---|---|---|
| `Main.main` | `Main.java` 7 a 96 | 90 | 15 | 1 |
| `registrarSolicitud` | 29 a 91 | 61 | **20** | **7** |
| `cambiarEstado` | 151 a 206 | 56 | 15 | 2 |
| `reporte` | 322 a 369 | 48 | 13 | 0 |
| `asignarResponsable` | 208 a 253 | 46 | 13 | 2 |
| `cargar` | 108 a 148 | 41 | 9 | 0 |
| `guardar` | 93 a 106 | 13 | 3 | 0 |
| `buscarPorEstado` / `buscarPorTipo` / `buscarPorLegajo` | 256 a 293 | 12 c/u | 4 c/u | 1 |
| `dias` | 296 a 306 | 11 | 4 | 1 |
| `esUrgente` | 314 a 320 | 7 | 5 | 1 |
| `agregarResponsable` | 20 a 26 | 7 | 1 | 2 |
| `calcularVencimiento` | 308 a 312 | 5 | 1 | 1 |
| `imprimirDebug` | 371 a 373 | 3 | 1 | 1 |

Resumen: 15 funciones, promedio de 28 líneas de código y complejidad promedio de 7,5. La clase `GestorSolicitudes` tiene 350 líneas de código y 14 métodos.

---

## 3. Code smells y deuda técnica

**Cómo se calcula la prioridad:** impacto (qué tanto dificulta cambiar o probar el sistema) y riesgo (qué tan probable es que cause un error). Prioridad **Alta** = impacto y riesgo altos; **Media** = uno alto y otro medio; **Baja** = ambos bajos.
Las líneas son de `GestorSolicitudes.java` salvo que se diga otra cosa.

| # | Smell | Ubicación | Evidencia | Impacto | Riesgo | Prioridad |
|---|---|---|---|---|---|---|
| S1 | **God Class** | Toda la clase `GestorSolicitudes`, líneas 12 a 374 | 350 líneas de código y 14 métodos que validan, aplican reglas, guardan en archivo, buscan, calculan y muestran por pantalla. | Alto | Alto | **Alta** |
| S2 | **Long Method** | `registrarSolicitud` (29 a 91), `cambiarEstado` (151 a 206), `reporte` (322 a 369), `Main.main` (7 a 96) | 61, 56, 48 y 90 líneas de código. `registrarSolicitud` tiene CCN 20. | Alto | Medio | **Alta** |
| S3 | **Anidamiento profundo (arrow code)** | `cambiarEstado`, líneas 151 a 206 | `if` dentro de `else` dentro de `else`, hasta 7 niveles. Las líneas 186 a 192 están anidadas dentro de la rama de RESUELTA. | Alto | Alto | **Alta** |
| S4 | **Long Parameter List y Data Clumps** | `registrarSolicitud`, línea 29 | 7 parámetros `String` (`nombre`, `legajo`, `email`, `tipo`, `descripcion`, `prioridad`, `fecha`). Se pasan siempre juntos desde `Main` (líneas 32 a 46). | Medio | Medio | Media |
| S5 | **Primitive Obsession** | `Solicitud.java` líneas 6 a 12 y todo el código | `estado`, `tipo`, `prioridad`, `responsable` y `area` son `String`. `"CERRADA"` aparece 6 veces, `"EN_PROCESO"` y `"RESUELTA"` 4 veces cada una. Un error de tipeo no lo detecta el compilador. | Alto | Medio | **Alta** |
| S6 | **Código duplicado** | Líneas 256 a 293, 324 a 347, 154 y 211, 189 y 244 | Tres métodos de búsqueda idénticos salvo la condición; cuatro bucles de conteo iguales en `reporte`; el bucle para buscar una solicitud por id está copiado en `cambiarEstado` y `asignarResponsable`; el código que descuenta la carga del responsable está copiado dos veces; la expresión `(s.responsable == null ? "sin asignar" : s.responsable)` aparece 4 veces. | Alto | Medio | **Alta** |
| S7 | **Cadenas if/else por tipo (Switch Statements)** | Líneas 78 a 86 (`registrarSolicitud`), 296 a 306 (`dias`), 314 a 320 (`esUrgente`) | El área, los días de plazo y la urgencia dependen del tipo y cada uno lo resuelve con su propio `if/else`. Un tipo nuevo obliga a tocar tres lugares. | Medio | Medio | Media |
| S8 | **Números y textos mágicos** | Línea 49 (`10`), 237 (`3`), 296 a 306 (`5`, `15`, `10`, `7`), línea 17 (`"solicitudes.csv"`) | Valores de negocio escritos directo en el código sin nombre ni explicación. | Bajo | Medio | Baja |
| S9 | **Estado global estático mutable** | Líneas 14 a 18 | `lista` y `responsables` son `public static` (cualquier clase puede modificarlas), `contador` y `archivo` también son estáticos y todos los métodos son `static`. Cualquier parte del programa puede modificarlos y dos pruebas se pisarían entre sí. | Alto | Alto | **Alta** |
| S10 | **Excepciones tragadas y persistencia frágil** | `guardar` (93 a 106) y `cargar` (108 a 148), líneas 103 a 105 y 146 | `catch (IOException e) { // nada }` y `catch (Exception e) {}`. Además los archivos no se cierran si hay un error (sin `try-with-resources`) y el CSV no escapa el `;`. Esto causó la falla reproducida en la etapa 2. | Alto | Alto | **Alta** |
| S11 | **Feature Envy** | `esUrgente` (314), `calcularVencimiento` (308) y `dias` (296) | Usan solamente datos de `Solicitud` pero viven en `GestorSolicitudes`. | Medio | Bajo | Media |
| S12 | **Lógica pegada a la consola (acoplamiento)** | Todo `GestorSolicitudes` | 38 `System.out.println` adentro de las reglas de negocio. Los métodos avisan los errores imprimiendo y devolviendo `false` o `-1`, en vez de lanzar excepciones. No se pueden probar sin leer la pantalla. | Alto | Medio | **Alta** |
| S13 | **Código muerto y nombres poco claros** | Líneas 18 (`x`), 43 (línea comentada), 371 a 373 (`imprimirDebug`) y los nombres de la sección 1.2 | `x` y `imprimirDebug` no se usan en ningún lado, hay código comentado y variables de una letra. | Bajo | Bajo | Baja |
| S14 | **Modelo anémico con campos públicos** | `Estudiante`, `Responsable`, `Solicitud` | Los campos son `public`, sin validación ni comportamiento. Las reglas que les corresponden quedaron en el gestor. | Medio | Medio | Media |

### Mapa de la deuda técnica

Los costos son una estimación aproximada del grupo, para ordenar el trabajo.

| Tipo de deuda | Smells | Costo de pagarla (aprox.) | Qué pasa si no se paga (el "interés") |
|---|---|---|---|
| Diseño | S1, S9, S12, S14 | 6 a 8 horas | Cada función nueva toca la misma clase gigante y no se puede probar por separado. |
| Código | S2, S3, S6, S8, S13 | 4 a 5 horas | Un cambio hay que repetirlo en varios lugares y es fácil olvidarse de uno. |
| Tipos | S5, S7, S11 | 3 horas | Un error de tipeo en un estado pasa el compilador y falla recién al ejecutar. |
| Errores y datos | S10 | 2 horas | Pérdida de datos sin avisar y caídas del programa (ya reproducido). |
| Pruebas | Sin pruebas | 3 horas | Se refactoriza sin red de seguridad. |
| Documentación | README básico | 1 a 2 horas | Cuesta entender decisiones de diseño. |

---

## 4. SOLID y Clean Code

### 4.1 SOLID

| Principio | ¿Se cumple en la v1? | Dónde se ve | Cómo se mejora |
|---|---|---|---|
| **SRP** (una sola razón para cambiar) | No | `GestorSolicitudes` cambia si cambian las reglas de negocio, el formato del archivo, el formato del reporte o los mensajes. `Main` mezcla menú, lectura de datos y llamadas. | Separar en clases: servicio de solicitudes, repositorio, reporte y menú. |
| **OCP** (abierto a extensión, cerrado a modificación) | No | Agregar un tipo de solicitud o un estado obliga a modificar los `if/else` de `registrarSolicitud`, `dias`, `esUrgente` y `cambiarEstado`. | Representar tipos y estados con `enum` que lleven sus propios datos (área, días). |
| **LSP** (sustitución) | No aplica | No hay herencia en la v1. | En la v2 las implementaciones del repositorio (archivo y memoria) tienen que poder reemplazarse sin cambiar el resto. |
| **ISP** (interfaces chicas) | No aplica | No hay interfaces en la v1. | En la v2 la interfaz del repositorio va a tener solo las operaciones que se usan. |
| **DIP** (depender de abstracciones) | No | El gestor usa directamente `FileWriter`, `FileReader`, `System.out` y un nombre de archivo fijo. | Que dependa de una interfaz de repositorio, así las pruebas usan una versión en memoria. |

### 4.2 Clean Code

| Regla | Cómo se rompe en la v1 | Dónde |
|---|---|---|
| Nombres que dicen lo que son | `x`, `n`, `a`, `t`, `p`, `e`, `r`, `c`, `rr` | S13 |
| Funciones chicas que hacen una sola cosa | Métodos de 46 a 90 líneas que validan, deciden y encima imprimen | S2, S3 |
| No repetirse (DRY) | Búsquedas, conteos y buscar-por-id copiados | S6 |
| Sin números ni textos mágicos | `3`, `10`, `5`, `15`, `"CERRADA"` escritos directo | S5, S8 |
| Manejo de errores | Se tragan las excepciones; los errores se avisan con `System.out` y `false` / `-1` | S10, S12 |
| Sin código muerto ni comentado | `imprimirDebug`, `x`, línea comentada | S13 |

---

## 5. Plan inicial de refactoring

Este plan se ajusta durante la etapa 5. Cada refactoring va en un commit propio.

| Refactoring | Técnica | Smells que ataca | Quién |
|---|---|---|---|
| R1 | **Extract Method**: partir `cambiarEstado` y `reporte` en métodos chicos (`buscarSolicitud`, `liberarCarga`, `contarPorEstado`) | S2, S3, S6 | Angel |
| R2 | **Introduce Parameter Object**: un `record DatosSolicitud` en lugar de los 7 parámetros | S4 | Angel |
| R3 | **Replace Type Code with Enum**: `Estado`, `TipoSolicitud` (con área y días) y `Prioridad` | S5, S7, S8 | Leandro |
| R4 | **Move Method**: `esUrgente` y `calcularVencimiento` pasan a `Solicitud`; la carga pasa a `Responsable` | S11, S14 | Leandro |
| R5 | **Extract Class**: sacar la persistencia a un repositorio y el reporte a su propia clase, sin estado estático | S1, S9, S10, S12 | A definir |

---

*Las métricas se calcularon con Lizard. La redacción de este documento se hizo con ayuda de Gemini IA; el grupo realizo y probó el sistema. El detalle del uso de IA está en el informe del TPI.*