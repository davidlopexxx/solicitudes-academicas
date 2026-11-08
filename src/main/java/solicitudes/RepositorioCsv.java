package solicitudes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// guarda las solicitudes en un archivo csv: una solicitud por linea, con los campos separados por ;
public class RepositorioCsv implements RepositorioSolicitudes {

    private static final int CANTIDAD_CAMPOS = 11;

    private final Path archivo;

    public RepositorioCsv(String nombreArchivo) {
        this.archivo = Path.of(nombreArchivo);
    }

    @Override
    public List<Solicitud> cargar() {
        List<Solicitud> solicitudes = new ArrayList<>();
        if (!Files.exists(archivo)) {
            return solicitudes;
        }
        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea = lector.readLine();
            int numeroLinea = 1;
            while (linea != null) {
                solicitudes.add(leerLinea(linea, numeroLinea));
                linea = lector.readLine();
                numeroLinea++;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo " + archivo, e);
        }
        return solicitudes;
    }

    @Override
    public void guardar(List<Solicitud> solicitudes) {
        try (BufferedWriter escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            for (Solicitud s : solicitudes) {
                escritor.write(armarLinea(s));
                escritor.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo " + archivo, e);
        }
    }

    private String armarLinea(Solicitud s) {
        return String.join(";",
                String.valueOf(s.id),
                codificar(s.estudiante.nombre),
                codificar(s.estudiante.legajo),
                codificar(s.estudiante.email),
                s.tipo.name(),
                codificar(s.descripcion),
                s.prioridad.name(),
                s.estado.name(),
                s.fechaCreacion,
                String.valueOf(s.responsable),
                s.area);
    }

    private Solicitud leerLinea(String linea, int numeroLinea) {
        String[] t = linea.split(";", -1);
        if (t.length != CANTIDAD_CAMPOS) {
            throw new IllegalStateException("La linea " + numeroLinea + " del archivo " + archivo
                    + " tiene " + t.length + " campos y deberia tener " + CANTIDAD_CAMPOS);
        }
        try {
            Solicitud s = new Solicitud();
            Estudiante est = new Estudiante();
            s.id = Integer.parseInt(t[0]);
            est.nombre = decodificar(t[1]);
            est.legajo = decodificar(t[2]);
            est.email = decodificar(t[3]);
            s.estudiante = est;
            s.tipo = TipoSolicitud.valueOf(t[4]);
            s.descripcion = decodificar(t[5]);
            s.prioridad = Prioridad.valueOf(t[6]);
            s.estado = Estado.valueOf(t[7]);
            s.fechaCreacion = t[8];
            s.responsable = t[9].equals("null") ? null : t[9];
            s.area = t[10];
            return s;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("La linea " + numeroLinea + " del archivo " + archivo
                    + " tiene datos invalidos", e);
        }
    }

    // para que un ; escrito por el usuario no se confunda con el separador de campos
    private String codificar(String texto) {
        return texto.replace("%", "%25").replace(";", "%3B");
    }

    private String decodificar(String texto) {
        return texto.replace("%3B", ";").replace("%25", "%");
    }
}