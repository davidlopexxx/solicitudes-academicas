package solicitudes;

public enum Prioridad {
    ALTA, MEDIA, BAJA;

    // devuelve la prioridad que se llama como el texto, o null si no existe
    public static Prioridad desdeTexto(String texto) {
        for (Prioridad p : values()) {
            if (p.name().equals(texto)) {
                return p;
            }
        }
        return null;
    }
}