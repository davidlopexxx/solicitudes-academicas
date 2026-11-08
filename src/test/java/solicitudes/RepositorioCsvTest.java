package solicitudes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepositorioCsvTest {

    private RepositorioCsv repositorioEn(Path carpeta) {
        return new RepositorioCsv(carpeta.resolve("solicitudes.csv").toString());
    }

    private Solicitud solicitud(int id, String nombre, String descripcion, String responsable) {
        Estudiante e = new Estudiante();
        e.nombre = nombre;
        e.legajo = "12345";
        e.email = "alumno@example.com";
        Solicitud s = new Solicitud();
        s.id = id;
        s.estudiante = e;
        s.tipo = TipoSolicitud.CERTIFICADO;
        s.descripcion = descripcion;
        s.prioridad = Prioridad.ALTA;
        s.estado = Estado.PENDIENTE;
        s.fechaCreacion = "03/11/2026";
        s.responsable = responsable;
        s.area = "Alumnado";
        return s;
    }

    @Test
    void sinArchivoSeDevuelveUnaListaVacia(@TempDir Path carpeta) {
        assertTrue(repositorioEn(carpeta).cargar().isEmpty());
    }

    @Test
    void lasSolicitudesGuardadasVuelvenIgualesAlCargarlas(@TempDir Path carpeta) {
        RepositorioCsv repositorio = repositorioEn(carpeta);
        List<Solicitud> originales = new ArrayList<>();
        originales.add(solicitud(1, "Maria Lopez", "Necesito un certificado de alumno regular", "Ana Gomez"));
        originales.add(solicitud(2, "Juan Perez", "Otra descripcion bastante larga", null));
        repositorio.guardar(originales);

        List<Solicitud> leidas = repositorio.cargar();
        assertEquals(2, leidas.size());
        assertEquals(1, leidas.get(0).id);
        assertEquals("Maria Lopez", leidas.get(0).estudiante.nombre);
        assertEquals("Ana Gomez", leidas.get(0).responsable);
        assertEquals(TipoSolicitud.CERTIFICADO, leidas.get(0).tipo);
        assertEquals(Estado.PENDIENTE, leidas.get(0).estado);
        assertEquals("03/11/2026", leidas.get(0).fechaCreacion);
        assertNull(leidas.get(1).responsable);
    }

    @Test
    void elPuntoYComaYElPorcentajeSeConservan(@TempDir Path carpeta) {
        RepositorioCsv repositorio = repositorioEn(carpeta);
        List<Solicitud> originales = new ArrayList<>();
        originales.add(solicitud(1, "Perez; Juan", "Descuento del 50% y %3B raro; con punto y coma", null));
        repositorio.guardar(originales);

        Solicitud leida = repositorio.cargar().get(0);
        assertEquals("Perez; Juan", leida.estudiante.nombre);
        assertEquals("Descuento del 50% y %3B raro; con punto y coma", leida.descripcion);
    }

    @Test
    void sePoneUnaSolicitudPorLinea(@TempDir Path carpeta) throws IOException {
        RepositorioCsv repositorio = repositorioEn(carpeta);
        List<Solicitud> originales = new ArrayList<>();
        originales.add(solicitud(1, "Maria Lopez", "Necesito un certificado de alumno regular", null));
        originales.add(solicitud(2, "Juan Perez", "Otra descripcion bastante larga", null));
        repositorio.guardar(originales);

        List<String> lineas = Files.readAllLines(carpeta.resolve("solicitudes.csv"), StandardCharsets.UTF_8);
        assertEquals(2, lineas.size());
    }

    @Test
    void unaLineaConCantidadDeCamposIncorrectaLanzaExcepcion(@TempDir Path carpeta) throws IOException {
        Files.writeString(carpeta.resolve("solicitudes.csv"), "esto;no;sirve\n", StandardCharsets.UTF_8);
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> repositorioEn(carpeta).cargar());
        assertTrue(error.getMessage().contains("linea 1"));
    }

    @Test
    void unEstadoInvalidoLanzaExcepcion(@TempDir Path carpeta) throws IOException {
        String linea = "1;Ana;1;a@b.com;CERTIFICADO;Descripcion larga;ALTA;ESTADO_RARO;03/11/2026;null;Alumnado\n";
        Files.writeString(carpeta.resolve("solicitudes.csv"), linea, StandardCharsets.UTF_8);
        assertThrows(IllegalStateException.class, () -> repositorioEn(carpeta).cargar());
    }
}