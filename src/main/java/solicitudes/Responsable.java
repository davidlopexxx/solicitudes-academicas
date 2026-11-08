package solicitudes;

public class Responsable {
    private static final int MAXIMO_ACTIVAS = 3;

    public String nombre;
    public String area;
    public int cargaActual;

    // un responsable no puede tener mas de 3 solicitudes activas
    public boolean puedeRecibirOtra() {
        return cargaActual < MAXIMO_ACTIVAS;
    }

    public void sumarCarga() {
        cargaActual = cargaActual + 1;
    }

    public void restarCarga() {
        cargaActual = cargaActual - 1;
    }
}