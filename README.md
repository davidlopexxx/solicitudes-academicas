# Sistema de Gestión de Solicitudes Académicas

Trabajo Práctico Integrador (TPI) de **Metodología de Sistemas II** - Tecnicatura Universitaria en Programación a Distancia (UTN).

**Integrantes:** Lopez Angel, Lopez Leandro

## Descripción

Aplicación de consola en Java que permite gestionar las solicitudes de los estudiantes de una facultad
(certificados, equivalencias, reincorporaciones y otros trámites). Los datos que se usan son inventados.

## Funcionalidades

1. **Registrar una solicitud** con los datos del estudiante, el tipo, la descripción, la prioridad y la fecha.
2. **Cambiar el estado** de una solicitud (PENDIENTE, EN_PROCESO, RESUELTA, CERRADA), respetando las transiciones permitidas.
3. **Asignar un responsable** del área que corresponde al tipo de solicitud (máximo 3 solicitudes activas por responsable).
4. **Buscar solicitudes** por estado, por tipo o por legajo.
5. **Reporte** con totales por estado, detalle de cada solicitud (vencimiento y urgencia) y carga de cada responsable.

Los datos se guardan en un archivo `solicitudes.csv` que se crea al ejecutar el programa.

## Tecnologías

- Java 17 o superior (se probó con Java 21)
- Maven
- JUnit 5 (para las pruebas de las etapas siguientes)

## Cómo ejecutarlo

Desde IntelliJ IDEA: abrir la carpeta del repositorio, esperar a que Maven importe el proyecto y ejecutar la clase `solicitudes.Main`.

Para repetir el escenario de demostración desde una terminal de Windows (en la carpeta del proyecto, con el proyecto ya compilado):

```
del solicitudes.csv
cmd /c "java -cp target\classes solicitudes.Main < docs\escenario_demo_entrada.txt"
```

## Estado del repositorio

Este repositorio sigue las etapas del TPI y cada hito está marcado con un tag:

| Tag | Qué marca |
|---|---|
| `estado-inicial` | Sistema base (versión inicial, con deuda técnica para diagnosticar) |

*(Los demás tags se van agregando a medida que avanzan las etapas.)*