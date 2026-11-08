package solicitudes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GestorSolicitudes {

    private final RepositorioSolicitudes repositorio;
    private final List<Solicitud> lista = new ArrayList<>();
    private final List<Responsable> responsables = new ArrayList<>();
    private int contador = 0;

    public GestorSolicitudes(RepositorioSolicitudes repositorio) {
        this.repositorio = repositorio;
    }

    public void agregarResponsable(String nombre, String area) {
        Responsable r = new Responsable();
        r.nombre = nombre;
        r.area = area;
        r.cargaActual = 0;
        responsables.add(r);
    }

    // lee las solicitudes guardadas y recalcula cuantas solicitudes activas tiene cada responsable
    public void cargar() {
        lista.clear();
        lista.addAll(repositorio.cargar());
        for (Solicitud s : lista) {
            if (s.id > contador) {
                contador = s.id;
            }
            if (s.responsable != null && s.estado != Estado.CERRADA) {
                Responsable r = buscarResponsable(s.responsable);
                if (r != null) {
                    r.sumarCarga();
                }
            }
        }
    }

    private void guardar() {
        repositorio.guardar(lista);
    }

    // listas de solo lectura, para que el reporte las pueda recorrer sin modificarlas
    public List<Solicitud> getSolicitudes() {
        return Collections.unmodifiableList(lista);
    }

    public List<Responsable> getResponsables() {
        return Collections.unmodifiableList(responsables);
    }

    // registra una solicitud nueva, devuelve el id o -1 si hubo un error
    public int registrarSolicitud(DatosSolicitud datos) {
        if (datos.nombre() == null || datos.nombre().trim().equals("")) {
            System.out.println("Error: el nombre no puede estar vacio");
            return -1;
        }
        if (datos.legajo() == null || datos.legajo().trim().equals("")) {
            System.out.println("Error: el legajo no puede estar vacio");
            return -1;
        }
        if (datos.email() == null || !datos.email().contains("@")) {
            System.out.println("Error: email invalido");
            return -1;
        }
        TipoSolicitud tipo = TipoSolicitud.desdeTexto(datos.tipo());
        if (tipo == null) {
            System.out.println("Error: tipo invalido");
            return -1;
        }
        if (datos.descripcion() == null || datos.descripcion().length() < 10) {
            System.out.println("Error: la descripcion debe tener al menos 10 caracteres");
            return -1;
        }
        Prioridad prioridad = Prioridad.desdeTexto(datos.prioridad());
        if (prioridad == null) {
            System.out.println("Error: prioridad invalida");
            return -1;
        }
        try {
            LocalDate.parse(datos.fecha(), Solicitud.FORMATO_FECHA);
        } catch (Exception e) {
            System.out.println("Error: fecha invalida, use dd/MM/yyyy");
            return -1;
        }
        Estudiante est = new Estudiante();
        est.nombre = datos.nombre();
        est.legajo = datos.legajo();
        est.email = datos.email();
        Solicitud s = new Solicitud();
        contador = contador + 1;
        s.id = contador;
        s.estudiante = est;
        s.tipo = tipo;
        s.descripcion = datos.descripcion();
        s.prioridad = prioridad;
        s.estado = Estado.PENDIENTE;
        s.fechaCreacion = datos.fecha();
        s.responsable = null;
        s.area = tipo.getArea();
        lista.add(s);
        guardar();
        System.out.println("Solicitud registrada con id " + s.id + " (area: " + s.area + ")");
        return s.id;
    }

    public boolean cambiarEstado(int id, String nuevoEstado) {
        Solicitud s = buscarSolicitud(id);
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return false;
        }
        if (!transicionValida(s, nuevoEstado)) {
            return false;
        }
        s.estado = Estado.desdeTexto(nuevoEstado);
        if (s.estado == Estado.CERRADA) {
            liberarCarga(s.responsable);
        }
        guardar();
        System.out.println("Solicitud " + id + " ahora esta " + s.estado);
        return true;
    }

    public boolean asignarResponsable(int id, String nombreResp) {
        Solicitud s = buscarSolicitud(id);
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return false;
        }
        Responsable r = buscarResponsable(nombreResp);
        if (r == null) {
            System.out.println("Error: no existe el responsable " + nombreResp);
            return false;
        }
        if (s.estado == Estado.CERRADA) {
            System.out.println("Error: no se puede asignar una solicitud cerrada");
            return false;
        }
        if (!r.area.equals(s.area)) {
            System.out.println("Error: " + r.nombre + " es del area " + r.area + " y la solicitud es de " + s.area);
            return false;
        }
        if (!r.puedeRecibirOtra()) {
            System.out.println("Error: " + r.nombre + " ya tiene 3 solicitudes activas");
            return false;
        }
        if (s.responsable != null) {
            liberarCarga(s.responsable);
        }
        s.responsable = r.nombre;
        r.sumarCarga();
        guardar();
        System.out.println("Solicitud " + id + " asignada a " + r.nombre);
        return true;
    }

    // ---- metodos auxiliares (salen de los metodos largos de arriba) ----

    // busca una solicitud por su id, devuelve null si no existe
    private Solicitud buscarSolicitud(int id) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).id == id) {
                return lista.get(i);
            }
        }
        return null;
    }

    // busca un responsable por su nombre, devuelve null si no existe
    private Responsable buscarResponsable(String nombre) {
        for (int i = 0; i < responsables.size(); i++) {
            if (responsables.get(i).nombre.equals(nombre)) {
                return responsables.get(i);
            }
        }
        return null;
    }

    // le baja en 1 la carga al responsable con ese nombre
    private void liberarCarga(String nombreResp) {
        Responsable r = buscarResponsable(nombreResp);
        if (r != null) {
            r.restarCarga();
        }
    }

    // controla si la solicitud puede pasar al nuevo estado; si no puede, imprime el motivo
    private boolean transicionValida(Solicitud s, String textoNuevoEstado) {
        if (s.estado == Estado.CERRADA) {
            System.out.println("Error: la solicitud ya esta cerrada");
            return false;
        }
        Estado nuevo = Estado.desdeTexto(textoNuevoEstado);
        if (nuevo == null || !s.estado.puedePasarA(nuevo)) {
            System.out.println("Error: transicion no permitida de " + s.estado + " a " + textoNuevoEstado);
            return false;
        }
        if (s.estado == Estado.PENDIENTE && s.responsable == null) {
            System.out.println("Error: no se puede pasar a EN_PROCESO sin responsable asignado");
            return false;
        }
        return true;
    }
}