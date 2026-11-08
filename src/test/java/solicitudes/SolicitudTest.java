package solicitudes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// pruebas de los metodos que se movieron a Solicitud en el refactoring 4
public class SolicitudTest {

    private Solicitud solicitud(TipoSolicitud tipo, Prioridad prioridad, Estado estado, String fecha) {
        Solicitud s = new Solicitud();
        s.tipo = tipo;
        s.prioridad = prioridad;
        s.estado = estado;
        s.fechaCreacion = fecha;
        return s;
    }

    @Test
    void elCertificadoVenceALos5Dias() {
        Solicitud s = solicitud(TipoSolicitud.CERTIFICADO, Prioridad.MEDIA, Estado.PENDIENTE, "03/11/2026");
        assertEquals("08/11/2026", s.calcularVencimiento());
    }

    @Test
    void laEquivalenciaVenceALos15Dias() {
        Solicitud s = solicitud(TipoSolicitud.EQUIVALENCIA, Prioridad.MEDIA, Estado.PENDIENTE, "10/11/2026");
        assertEquals("25/11/2026", s.calcularVencimiento());
    }

    @Test
    void elVencimientoPasaAlMesSiguiente() {
        Solicitud s = solicitud(TipoSolicitud.CERTIFICADO, Prioridad.MEDIA, Estado.PENDIENTE, "28/11/2026");
        assertEquals("03/12/2026", s.calcularVencimiento());
    }

    @Test
    void unaSolicitudDePrioridadAltaPendienteEsUrgente() {
        assertTrue(solicitud(TipoSolicitud.CERTIFICADO, Prioridad.ALTA, Estado.PENDIENTE, "03/11/2026").esUrgente());
    }

    @Test
    void unaReincorporacionEsUrgenteAunqueSeaDePrioridadBaja() {
        assertTrue(solicitud(TipoSolicitud.REINCORPORACION, Prioridad.BAJA, Estado.EN_PROCESO, "03/11/2026").esUrgente());
    }

    @Test
    void unaSolicitudComunNoEsUrgente() {
        assertFalse(solicitud(TipoSolicitud.CERTIFICADO, Prioridad.MEDIA, Estado.PENDIENTE, "03/11/2026").esUrgente());
    }

    @Test
    void unaSolicitudResueltaOCerradaYaNoEsUrgente() {
        assertFalse(solicitud(TipoSolicitud.CERTIFICADO, Prioridad.ALTA, Estado.RESUELTA, "03/11/2026").esUrgente());
        assertFalse(solicitud(TipoSolicitud.REINCORPORACION, Prioridad.ALTA, Estado.CERRADA, "03/11/2026").esUrgente());
    }
}