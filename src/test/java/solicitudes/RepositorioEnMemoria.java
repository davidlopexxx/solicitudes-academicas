package solicitudes;

import java.util.ArrayList;
import java.util.List;

// repositorio falso para las pruebas: guarda las solicitudes en una lista en vez de en un archivo
public class RepositorioEnMemoria implements RepositorioSolicitudes {

    public List<Solicitud> guardadas = new ArrayList<>();
    public int vecesGuardado = 0;

    @Override
    public List<Solicitud> cargar() {
        return new ArrayList<>(guardadas);
    }

    @Override
    public void guardar(List<Solicitud> solicitudes) {
        guardadas = new ArrayList<>(solicitudes);
        vecesGuardado++;
    }
}