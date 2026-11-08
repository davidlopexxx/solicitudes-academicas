package solicitudes;

public enum TipoSolicitud {
    CERTIFICADO("Alumnado", 5),
    EQUIVALENCIA("Academica", 15),
    REINCORPORACION("Alumnado", 10),
    OTRO("Mesa de Entradas", 7);

    private final String area;
    private final int dias;

    // cada tipo sabe a que area va y cuantos dias tiene la oficina para resolverlo
    TipoSolicitud(String area, int dias) {
        this.area = area;
        this.dias = dias;
    }

    public String getArea() {
        return area;
    }

    public int getDias() {
        return dias;
    }

    // devuelve el tipo que se llama como el texto, o null si no existe
    public static TipoSolicitud desdeTexto(String texto) {
        for (TipoSolicitud t : values()) {
            if (t.name().equals(texto)) {
                return t;
            }
        }
        return null;
    }
}