package solicitudes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Solicitud {
    public int id;
    public Estudiante estudiante;
    public TipoSolicitud tipo;
    public String descripcion;
    public Prioridad prioridad;
    public Estado estado;
    public String fechaCreacion;
    public String responsable;
    public String area;

    // fecha limite para resolverla: la fecha de creacion mas los dias que corresponden al tipo
    public String calcularVencimiento() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate d = LocalDate.parse(fechaCreacion, f);
        return d.plusDays(tipo.getDias()).format(f);
    }

    // es urgente si es de prioridad alta o es una reincorporacion, y todavia no se resolvio
    public boolean esUrgente() {
        return (prioridad == Prioridad.ALTA || tipo == TipoSolicitud.REINCORPORACION)
                && estado != Estado.RESUELTA && estado != Estado.CERRADA;
    }
}