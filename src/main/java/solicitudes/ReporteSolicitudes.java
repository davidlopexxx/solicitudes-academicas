package solicitudes;

import java.util.List;
import java.util.function.Predicate;

// arma el reporte general y las busquedas: lee las solicitudes y los responsables pero no los modifica
public class ReporteSolicitudes {

    private final List<Solicitud> solicitudes;
    private final List<Responsable> responsables;

    public ReporteSolicitudes(List<Solicitud> solicitudes, List<Responsable> responsables) {
        this.solicitudes = solicitudes;
        this.responsables = responsables;
    }

    public void buscarPorEstado(String estado) {
        buscar(s -> s.estado.name().equals(estado));
    }

    public void buscarPorTipo(String tipo) {
        buscar(s -> s.tipo.name().equals(tipo));
    }

    public void buscarPorLegajo(String legajo) {
        buscar(s -> s.estudiante.legajo.equals(legajo));
    }

    // imprime las solicitudes que cumplen la condicion y cuantas son
    private void buscar(Predicate<Solicitud> condicion) {
        int encontradas = 0;
        for (Solicitud s : solicitudes) {
            if (condicion.test(s)) {
                System.out.println("#" + s.id + " | " + s.estudiante.nombre + " | " + s.tipo + " | " + s.prioridad
                        + " | " + s.estado + " | " + textoResponsable(s));
                encontradas++;
            }
        }
        System.out.println("Total encontradas: " + encontradas);
    }

    public void imprimirReporte() {
        System.out.println("===== REPORTE DE SOLICITUDES =====");
        System.out.println("Total: " + solicitudes.size());
        System.out.println("Pendientes: " + contarPorEstado(Estado.PENDIENTE));
        System.out.println("En proceso: " + contarPorEstado(Estado.EN_PROCESO));
        System.out.println("Resueltas: " + contarPorEstado(Estado.RESUELTA));
        System.out.println("Cerradas: " + contarPorEstado(Estado.CERRADA));
        System.out.println("--- Detalle ---");
        for (Solicitud s : solicitudes) {
            String urgente = s.esUrgente() ? "SI" : "NO";
            System.out.println("#" + s.id + " | " + s.estudiante.nombre + " (" + s.estudiante.legajo + ") | " + s.tipo
                    + " | " + s.prioridad + " | " + s.estado + " | " + textoResponsable(s)
                    + " | vence " + s.calcularVencimiento() + " | urgente: " + urgente);
        }
        System.out.println("--- Carga de responsables ---");
        for (Responsable r : responsables) {
            System.out.println(r.nombre + " (" + r.area + "): " + r.cargaActual + " solicitudes activas");
        }
    }

    // cuenta cuantas solicitudes hay en un estado
    private int contarPorEstado(Estado estado) {
        int cantidad = 0;
        for (Solicitud s : solicitudes) {
            if (s.estado == estado) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // el nombre del responsable o "sin asignar" si todavia no tiene
    private String textoResponsable(Solicitud s) {
        return s.responsable == null ? "sin asignar" : s.responsable;
    }
}