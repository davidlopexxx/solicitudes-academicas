package solicitudes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// pruebas unitarias del gestor: usan el repositorio en memoria, asi no se toca ningun archivo
public class GestorSolicitudesTest {

    private RepositorioEnMemoria repositorio;
    private GestorSolicitudes gestor;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioEnMemoria();
        gestor = new GestorSolicitudes(repositorio);
        gestor.agregarResponsable("Ana Gomez", "Alumnado");
        gestor.agregarResponsable("Carlos Ruiz", "Academica");
    }

    private DatosSolicitud datos(String tipo) {
        return new DatosSolicitud("Maria Lopez", "12345", "maria.lopez@example.com", tipo,
                "Necesito un certificado de alumno regular", "ALTA", "03/11/2026");
    }

    private int cargaDe(String nombre) {
        for (Responsable r : gestor.getResponsables()) {
            if (r.nombre.equals(nombre)) {
                return r.cargaActual;
            }
        }
        return -1;
    }

    @Test
    void registrarGuardaLaSolicitudYDevuelveSuId() {
        int id = gestor.registrarSolicitud(datos("CERTIFICADO"));
        assertEquals(1, id);
        assertEquals(1, gestor.getSolicitudes().size());
        assertEquals(1, repositorio.guardadas.size());
        assertEquals(Estado.PENDIENTE, gestor.getSolicitudes().get(0).estado);
    }

    @Test
    void losIdsSonCorrelativos() {
        assertEquals(1, gestor.registrarSolicitud(datos("CERTIFICADO")));
        assertEquals(2, gestor.registrarSolicitud(datos("CERTIFICADO")));
    }

    @Test
    void elAreaDependeDelTipoDeSolicitud() {
        gestor.registrarSolicitud(datos("CERTIFICADO"));
        gestor.registrarSolicitud(datos("EQUIVALENCIA"));
        assertEquals("Alumnado", gestor.getSolicitudes().get(0).area);
        assertEquals("Academica", gestor.getSolicitudes().get(1).area);
    }

    @Test
    void conEmailInvalidoNoSeRegistraNiSeGuarda() {
        DatosSolicitud sinArroba = new DatosSolicitud("Maria Lopez", "12345", "maria.lopez.example.com",
                "CERTIFICADO", "Necesito un certificado de alumno regular", "ALTA", "03/11/2026");
        assertEquals(-1, gestor.registrarSolicitud(sinArroba));
        assertEquals(0, gestor.getSolicitudes().size());
        assertEquals(0, repositorio.vecesGuardado);
    }

    @Test
    void conNombreVacioNoSeRegistra() {
        DatosSolicitud sinNombre = new DatosSolicitud("   ", "12345", "maria.lopez@example.com",
                "CERTIFICADO", "Necesito un certificado de alumno regular", "ALTA", "03/11/2026");
        assertEquals(-1, gestor.registrarSolicitud(sinNombre));
        assertEquals(0, gestor.getSolicitudes().size());
    }

    @Test
    void conDescripcionCortaNoSeRegistra() {
        DatosSolicitud corta = new DatosSolicitud("Maria Lopez", "12345", "maria.lopez@example.com",
                "CERTIFICADO", "corta", "ALTA", "03/11/2026");
        assertEquals(-1, gestor.registrarSolicitud(corta));
    }

    @Test
    void conFechaInvalidaNoSeRegistra() {
        DatosSolicitud fechaMala = new DatosSolicitud("Maria Lopez", "12345", "maria.lopez@example.com",
                "CERTIFICADO", "Necesito un certificado de alumno regular", "ALTA", "2026-11-03");
        assertEquals(-1, gestor.registrarSolicitud(fechaMala));
    }

    @Test
    void conTipoInvalidoNoSeRegistra() {
        assertEquals(-1, gestor.registrarSolicitud(datos("INVENTADO")));
    }

    @Test
    void noSePuedePasarAEnProcesoSinResponsable() {
        gestor.registrarSolicitud(datos("CERTIFICADO"));
        assertFalse(gestor.cambiarEstado(1, "EN_PROCESO"));
        assertEquals(Estado.PENDIENTE, gestor.getSolicitudes().get(0).estado);
    }

    @Test
    void conResponsableSePuedePasarAEnProceso() {
        gestor.registrarSolicitud(datos("CERTIFICADO"));
        assertTrue(gestor.asignarResponsable(1, "Ana Gomez"));
        assertTrue(gestor.cambiarEstado(1, "EN_PROCESO"));
        assertEquals(Estado.EN_PROCESO, gestor.getSolicitudes().get(0).estado);
    }

    @Test
    void noSePuedenSaltearEstados() {
        gestor.registrarSolicitud(datos("CERTIFICADO"));
        gestor.asignarResponsable(1, "Ana Gomez");
        assertFalse(gestor.cambiarEstado(1, "RESUELTA"));
        assertEquals(Estado.PENDIENTE, gestor.getSolicitudes().get(0).estado);
    }

    @Test
    void unEstadoQueNoExisteSeRechaza() {
        gestor.registrarSolicitud(datos("CERTIFICADO"));
        assertFalse(gestor.cambiarEstado(1, "TERMINADA"));
    }

    @Test
    void unaSolicitudQueNoExisteSeRechaza() {
        assertFalse(gestor.cambiarEstado(99, "EN_PROCESO"));
        assertFalse(gestor.asignarResponsable(99, "Ana Gomez"));
    }

    @Test
    void unResponsableDeOtraAreaNoSePuedeAsignar() {
        gestor.registrarSolicitud(datos("EQUIVALENCIA"));
        assertFalse(gestor.asignarResponsable(1, "Ana Gomez"));
        assertEquals(0, cargaDe("Ana Gomez"));
    }

    @Test
    void unResponsableNoPuedeTenerMasDeTresSolicitudesActivas() {
        for (int i = 0; i < 4; i++) {
            gestor.registrarSolicitud(datos("CERTIFICADO"));
        }
        assertTrue(gestor.asignarResponsable(1, "Ana Gomez"));
        assertTrue(gestor.asignarResponsable(2, "Ana Gomez"));
        assertTrue(gestor.asignarResponsable(3, "Ana Gomez"));
        assertFalse(gestor.asignarResponsable(4, "Ana Gomez"));
        assertEquals(3, cargaDe("Ana Gomez"));
    }

    @Test
    void cerrarUnaSolicitudLiberaLaCargaDelResponsable() {
        gestor.registrarSolicitud(datos("CERTIFICADO"));
        gestor.asignarResponsable(1, "Ana Gomez");
        assertEquals(1, cargaDe("Ana Gomez"));
        gestor.cambiarEstado(1, "EN_PROCESO");
        gestor.cambiarEstado(1, "RESUELTA");
        assertTrue(gestor.cambiarEstado(1, "CERRADA"));
        assertEquals(0, cargaDe("Ana Gomez"));
    }

    @Test
    void unaSolicitudCerradaYaNoSePuedeModificar() {
        gestor.registrarSolicitud(datos("CERTIFICADO"));
        gestor.asignarResponsable(1, "Ana Gomez");
        gestor.cambiarEstado(1, "EN_PROCESO");
        gestor.cambiarEstado(1, "RESUELTA");
        gestor.cambiarEstado(1, "CERRADA");
        assertFalse(gestor.cambiarEstado(1, "EN_PROCESO"));
        assertFalse(gestor.asignarResponsable(1, "Ana Gomez"));
    }

    @Test
    void cadaCambioSeGuardaEnElRepositorio() {
        gestor.registrarSolicitud(datos("CERTIFICADO"));
        assertEquals(1, repositorio.vecesGuardado);
        gestor.asignarResponsable(1, "Ana Gomez");
        assertEquals(2, repositorio.vecesGuardado);
        gestor.cambiarEstado(1, "EN_PROCESO");
        assertEquals(3, repositorio.vecesGuardado);
    }

    @Test
    void alCargarSeRecuperanLasSolicitudesYLaCargaDeLosResponsables() {
        gestor.registrarSolicitud(datos("CERTIFICADO"));
        gestor.registrarSolicitud(datos("CERTIFICADO"));
        gestor.asignarResponsable(1, "Ana Gomez");

        // un gestor nuevo que arranca con lo que quedo guardado, como cuando se vuelve a abrir el programa
        GestorSolicitudes otro = new GestorSolicitudes(repositorio);
        otro.agregarResponsable("Ana Gomez", "Alumnado");
        otro.cargar();

        assertEquals(2, otro.getSolicitudes().size());
        assertEquals(1, otro.getResponsables().get(0).cargaActual);
        assertEquals(3, otro.registrarSolicitud(datos("CERTIFICADO")));
    }
}