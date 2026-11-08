package solicitudes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// el reporte se prueba solo, con listas armadas a mano, sin necesidad del gestor
public class ReporteSolicitudesTest {

    private PrintStream salidaOriginal;
    private ByteArrayOutputStream salida;
    private List<Solicitud> solicitudes;
    private List<Responsable> responsables;
    private ReporteSolicitudes reporte;

    @BeforeEach
    void preparar() {
        // se captura lo que se imprime por consola para poder revisarlo
        salidaOriginal = System.out;
        salida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(salida));

        solicitudes = new ArrayList<>();
        solicitudes.add(solicitud(1, "Maria Lopez", "12345", TipoSolicitud.CERTIFICADO, Prioridad.ALTA,
                Estado.PENDIENTE, null));
        solicitudes.add(solicitud(2, "Juan Perez", "23456", TipoSolicitud.EQUIVALENCIA, Prioridad.MEDIA,
                Estado.RESUELTA, "Carlos Ruiz"));

        responsables = new ArrayList<>();
        Responsable carlos = new Responsable();
        carlos.nombre = "Carlos Ruiz";
        carlos.area = "Academica";
        carlos.cargaActual = 1;
        responsables.add(carlos);

        reporte = new ReporteSolicitudes(solicitudes, responsables);
    }

    @AfterEach
    void restaurar() {
        System.setOut(salidaOriginal);
    }

    private Solicitud solicitud(int id, String nombre, String legajo, TipoSolicitud tipo, Prioridad prioridad,
                                Estado estado, String responsable) {
        Estudiante e = new Estudiante();
        e.nombre = nombre;
        e.legajo = legajo;
        e.email = "alumno@example.com";
        Solicitud s = new Solicitud();
        s.id = id;
        s.estudiante = e;
        s.tipo = tipo;
        s.prioridad = prioridad;
        s.estado = estado;
        s.fechaCreacion = "03/11/2026";
        s.responsable = responsable;
        s.area = tipo.getArea();
        return s;
    }

    private String texto() {
        return salida.toString();
    }

    @Test
    void buscarPorEstadoMuestraSoloLasQueCoinciden() {
        reporte.buscarPorEstado("PENDIENTE");
        assertTrue(texto().contains("#1 | Maria Lopez"));
        assertFalse(texto().contains("#2 | Juan Perez"));
        assertTrue(texto().contains("Total encontradas: 1"));
    }

    @Test
    void buscarPorTipoMuestraSoloLasQueCoinciden() {
        reporte.buscarPorTipo("EQUIVALENCIA");
        assertTrue(texto().contains("#2 | Juan Perez"));
        assertFalse(texto().contains("#1 | Maria Lopez"));
        assertTrue(texto().contains("Total encontradas: 1"));
    }

    @Test
    void buscarPorLegajoSinResultados() {
        reporte.buscarPorLegajo("99999");
        assertTrue(texto().contains("Total encontradas: 0"));
    }

    @Test
    void laBusquedaMuestraSinAsignarSiNoHayResponsable() {
        reporte.buscarPorLegajo("12345");
        assertTrue(texto().contains("sin asignar"));
    }

    @Test
    void elReporteCuentaLasSolicitudesPorEstado() {
        reporte.imprimirReporte();
        assertTrue(texto().contains("Total: 2"));
        assertTrue(texto().contains("Pendientes: 1"));
        assertTrue(texto().contains("En proceso: 0"));
        assertTrue(texto().contains("Resueltas: 1"));
        assertTrue(texto().contains("Cerradas: 0"));
    }

    @Test
    void elReporteMarcaLasUrgentesYMuestraLaCargaDeLosResponsables() {
        reporte.imprimirReporte();
        assertTrue(texto().contains("vence 08/11/2026 | urgente: SI"));
        assertTrue(texto().contains("urgente: NO"));
        assertTrue(texto().contains("Carlos Ruiz (Academica): 1 solicitudes activas"));
    }
}