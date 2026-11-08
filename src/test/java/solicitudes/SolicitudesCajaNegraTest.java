package solicitudes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SolicitudesCajaNegraTest {

    // corre el programa como lo haria un usuario: le pasa las respuestas por teclado
    // y devuelve todo lo que el programa imprime
    private String ejecutar(Path carpeta, String entrada) throws IOException, InterruptedException {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String clases = Path.of("target", "classes").toAbsolutePath().toString();
        ProcessBuilder pb = new ProcessBuilder(java, "-cp", clases, "solicitudes.Main");
        pb.directory(carpeta.toFile());
        pb.environment().remove("JAVA_TOOL_OPTIONS"); // para que no se cuelen mensajes extra en la salida
        pb.redirectErrorStream(true);
        Process p = pb.start();
        p.getOutputStream().write(entrada.getBytes(StandardCharsets.UTF_8));
        p.getOutputStream().close();
        String salida = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        p.waitFor();
        return salida.replace("\r\n", "\n");
    }

    private String leer(String ruta) throws IOException {
        return Files.readString(Path.of(ruta), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    // las respuestas que hay que escribir para registrar una solicitud de tipo CERTIFICADO
    private String registro(String nombre, String legajo, String email) {
        return String.join("\n", "1", nombre, legajo, email, "CERTIFICADO",
                "Necesito certificado de alumno regular", "ALTA", "03/11/2026") + "\n";
    }

    @Test
    void escenarioDemoCoincideConLaSalidaEsperada(@TempDir Path carpeta) throws Exception {
        String entrada = leer("docs/escenario_demo_entrada.txt");
        String esperado = leer("docs/evidencias/ejecucion_v1.txt");
        assertEquals(esperado, ejecutar(carpeta, entrada));
    }

    @Test
    void noSePuedePasarAEnProcesoSinResponsable(@TempDir Path carpeta) throws Exception {
        String entrada = registro("Maria Lopez", "12345", "maria.lopez@example.com")
                + "2\n1\nEN_PROCESO\n0\n";
        String salida = ejecutar(carpeta, entrada);
        assertTrue(salida.contains("Error: no se puede pasar a EN_PROCESO sin responsable asignado"));
    }

    @Test
    void unResponsableNoPuedeTenerMasDeTresSolicitudesActivas(@TempDir Path carpeta) throws Exception {
        StringBuilder entrada = new StringBuilder();
        entrada.append(registro("Alumno Uno", "1001", "uno@example.com"));
        entrada.append(registro("Alumno Dos", "1002", "dos@example.com"));
        entrada.append(registro("Alumno Tres", "1003", "tres@example.com"));
        entrada.append(registro("Alumno Cuatro", "1004", "cuatro@example.com"));
        for (int id = 1; id <= 4; id++) {
            entrada.append("3\n" + id + "\nAna Gomez\n");
        }
        entrada.append("0\n");
        String salida = ejecutar(carpeta, entrada.toString());
        assertTrue(salida.contains("Solicitud 3 asignada a Ana Gomez"));
        assertTrue(salida.contains("Error: Ana Gomez ya tiene 3 solicitudes activas"));
    }

    @Test
    void losDatosSeConservanAlVolverAAbrirElPrograma(@TempDir Path carpeta) throws Exception {
        ejecutar(carpeta, registro("Maria Lopez", "12345", "maria.lopez@example.com") + "0\n");
        String salida = ejecutar(carpeta, "5\n0\n");
        assertTrue(salida.contains("Total: 1"));
        assertTrue(salida.contains("#1 | Maria Lopez (12345) | CERTIFICADO"));
    }

    // el ; en el nombre ya no rompe el archivo: se guarda codificado y vuelve igual al leerlo
    @Test
    void nombreConPuntoYComaSeConservaAlVolverAAbrirElPrograma(@TempDir Path carpeta) throws Exception {
        ejecutar(carpeta, registro("Perez; Juan", "99999", "pj@example.com") + "0\n");
        String salida = ejecutar(carpeta, "5\n0\n");
        assertTrue(salida.contains("Total: 1"));
        assertTrue(salida.contains("#1 | Perez; Juan (99999) | CERTIFICADO"));
    }

    // si el archivo guardado esta danado, el programa avisa y no sigue (antes se perdian solicitudes sin aviso)
    @Test
    void unArchivoDanadoHaceQueElProgramaAvise(@TempDir Path carpeta) throws Exception {
        Files.writeString(carpeta.resolve("solicitudes.csv"), "esto no es una solicitud\n", StandardCharsets.UTF_8);
        String salida = ejecutar(carpeta, "5\n0\n");
        assertTrue(salida.contains("Error al leer los datos guardados"));
    }
}