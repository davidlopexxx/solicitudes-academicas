package solicitudes;

import java.util.List;

// define como se guardan y se leen las solicitudes, sin importar donde (archivo, memoria, base de datos)
public interface RepositorioSolicitudes {

    // devuelve todas las solicitudes guardadas (una lista vacia si todavia no hay ninguna)
    List<Solicitud> cargar();

    // guarda todas las solicitudes, reemplazando lo que habia
    void guardar(List<Solicitud> solicitudes);
}