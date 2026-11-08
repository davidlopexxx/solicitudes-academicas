package solicitudes;

// guarda juntos los datos que se necesitan para registrar una solicitud
public record DatosSolicitud(String nombre, String legajo, String email, String tipo,
                             String descripcion, String prioridad, String fecha) {
}