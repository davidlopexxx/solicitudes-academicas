package solicitudes;

public enum Estado {
    PENDIENTE, EN_PROCESO, RESUELTA, CERRADA;

    // dice si desde este estado se puede pasar al otro
    public boolean puedePasarA(Estado otro) {
        switch (this) {
            case PENDIENTE:
                return otro == EN_PROCESO;
            case EN_PROCESO:
                return otro == RESUELTA;
            case RESUELTA:
                return otro == CERRADA || otro == EN_PROCESO;
            default:
                return false;
        }
    }

    // devuelve el estado que se llama como el texto, o null si no existe
    public static Estado desdeTexto(String texto) {
        for (Estado e : values()) {
            if (e.name().equals(texto)) {
                return e;
            }
        }
        return null;
    }
}