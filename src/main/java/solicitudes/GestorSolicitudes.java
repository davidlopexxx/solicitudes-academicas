package solicitudes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class GestorSolicitudes {

    public static ArrayList<Solicitud> lista = new ArrayList<Solicitud>();
    public static ArrayList<Responsable> responsables = new ArrayList<Responsable>();
    static int contador = 0;
    static String archivo = "solicitudes.csv";
    static int x = 0;

    public static void agregarResponsable(String n, String a) {
        Responsable r = new Responsable();
        r.nombre = n;
        r.area = a;
        r.cargaActual = 0;
        responsables.add(r);
    }

    // registra una solicitud nueva, devuelve el id o -1 si hubo un error
    public static int registrarSolicitud(String nombre, String legajo, String email, String tipo,
                                         String descripcion, String prioridad, String fecha) {
        if (nombre == null || nombre.trim().equals("")) {
            System.out.println("Error: el nombre no puede estar vacio");
            return -1;
        }
        if (legajo == null || legajo.trim().equals("")) {
            System.out.println("Error: el legajo no puede estar vacio");
            return -1;
        }
        if (email == null || !email.contains("@")) {
            System.out.println("Error: email invalido");
            return -1;
        }
        // if (tipo == "CERTIFICADO" || tipo == "EQUIVALENCIA") {
        if (!(tipo.equals("CERTIFICADO") || tipo.equals("EQUIVALENCIA") || tipo.equals("REINCORPORACION")
                || tipo.equals("OTRO"))) {
            System.out.println("Error: tipo invalido");
            return -1;
        }
        if (descripcion == null || descripcion.length() < 10) {
            System.out.println("Error: la descripcion debe tener al menos 10 caracteres");
            return -1;
        }
        if (!(prioridad.equals("ALTA") || prioridad.equals("MEDIA") || prioridad.equals("BAJA"))) {
            System.out.println("Error: prioridad invalida");
            return -1;
        }
        try {
            LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            System.out.println("Error: fecha invalida, use dd/MM/yyyy");
            return -1;
        }
        Estudiante est = new Estudiante();
        est.nombre = nombre;
        est.legajo = legajo;
        est.email = email;
        Solicitud s = new Solicitud();
        contador = contador + 1;
        s.id = contador;
        s.estudiante = est;
        s.tipo = tipo;
        s.descripcion = descripcion;
        s.prioridad = prioridad;
        s.estado = "PENDIENTE";
        s.fechaCreacion = fecha;
        s.responsable = null;
        // el area depende del tipo
        if (tipo.equals("CERTIFICADO")) {
            s.area = "Alumnado";
        } else if (tipo.equals("EQUIVALENCIA")) {
            s.area = "Academica";
        } else if (tipo.equals("REINCORPORACION")) {
            s.area = "Alumnado";
        } else {
            s.area = "Mesa de Entradas";
        }
        lista.add(s);
        guardar();
        System.out.println("Solicitud registrada con id " + s.id + " (area: " + s.area + ")");
        return s.id;
    }

    public static void guardar() {
        try {
            FileWriter fw = new FileWriter(archivo);
            for (int i = 0; i < lista.size(); i++) {
                Solicitud s = lista.get(i);
                fw.write(s.id + ";" + s.estudiante.nombre + ";" + s.estudiante.legajo + ";" + s.estudiante.email + ";"
                        + s.tipo + ";" + s.descripcion.replace(";", ",") + ";" + s.prioridad + ";" + s.estado + ";"
                        + s.fechaCreacion + ";" + s.responsable + ";" + s.area + "\n");
            }
            fw.close();
        } catch (IOException e) {
            // nada
        }
    }

    public static void cargar() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea = br.readLine();
            while (linea != null) {
                String[] t = linea.split(";");
                Solicitud s = new Solicitud();
                Estudiante est = new Estudiante();
                s.id = Integer.parseInt(t[0]);
                est.nombre = t[1];
                est.legajo = t[2];
                est.email = t[3];
                s.estudiante = est;
                s.tipo = t[4];
                s.descripcion = t[5];
                s.prioridad = t[6];
                s.estado = t[7];
                s.fechaCreacion = t[8];
                if (t[9].equals("null")) {
                    s.responsable = null;
                } else {
                    s.responsable = t[9];
                }
                s.area = t[10];
                lista.add(s);
                if (s.id > contador) {
                    contador = s.id;
                }
                if (s.responsable != null && !s.estado.equals("CERRADA")) {
                    for (int i = 0; i < responsables.size(); i++) {
                        if (responsables.get(i).nombre.equals(s.responsable)) {
                            responsables.get(i).cargaActual = responsables.get(i).cargaActual + 1;
                        }
                    }
                }
                linea = br.readLine();
            }
            br.close();
        } catch (Exception e) {
        }
    }


    public static boolean cambiarEstado(int id, String nuevoEstado) {
        Solicitud s = null;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).id == id) {
                s = lista.get(i);
            }
        }
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return false;
        }
        if (s.estado.equals("PENDIENTE")) {
            if (nuevoEstado.equals("EN_PROCESO")) {
                if (s.responsable == null) {
                    System.out.println("Error: no se puede pasar a EN_PROCESO sin responsable asignado");
                    return false;
                } else {
                    s.estado = nuevoEstado;
                }
            } else {
                System.out.println("Error: transicion no permitida de PENDIENTE a " + nuevoEstado);
                return false;
            }
        } else {
            if (s.estado.equals("EN_PROCESO")) {
                if (nuevoEstado.equals("RESUELTA")) {
                    s.estado = nuevoEstado;
                } else {
                    System.out.println("Error: transicion no permitida de EN_PROCESO a " + nuevoEstado);
                    return false;
                }
            } else {
                if (s.estado.equals("RESUELTA")) {
                    if (nuevoEstado.equals("CERRADA") || nuevoEstado.equals("EN_PROCESO")) {
                        s.estado = nuevoEstado;
                        if (nuevoEstado.equals("CERRADA")) {
                            for (int i = 0; i < responsables.size(); i++) {
                                if (responsables.get(i).nombre.equals(s.responsable)) {
                                    responsables.get(i).cargaActual = responsables.get(i).cargaActual - 1;
                                }
                            }
                        }
                    } else {
                        System.out.println("Error: transicion no permitida de RESUELTA a " + nuevoEstado);
                        return false;
                    }
                } else {
                    System.out.println("Error: la solicitud ya esta cerrada");
                    return false;
                }
            }
        }
        guardar();
        System.out.println("Solicitud " + id + " ahora esta " + s.estado);
        return true;
    }

    public static boolean asignarResponsable(int id, String nombreResp) {
        Solicitud s = null;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).id == id) {
                s = lista.get(i);
            }
        }
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return false;
        }
        Responsable r = null;
        for (int i = 0; i < responsables.size(); i++) {
            if (responsables.get(i).nombre.equals(nombreResp)) {
                r = responsables.get(i);
            }
        }
        if (r == null) {
            System.out.println("Error: no existe el responsable " + nombreResp);
            return false;
        }
        if (s.estado.equals("CERRADA")) {
            System.out.println("Error: no se puede asignar una solicitud cerrada");
            return false;
        }
        if (!r.area.equals(s.area)) {
            System.out.println("Error: " + r.nombre + " es del area " + r.area + " y la solicitud es de " + s.area);
            return false;
        }
        if (r.cargaActual >= 3) {
            System.out.println("Error: " + r.nombre + " ya tiene 3 solicitudes activas");
            return false;
        }
        if (s.responsable != null) {
            for (int i = 0; i < responsables.size(); i++) {
                if (responsables.get(i).nombre.equals(s.responsable)) {
                    responsables.get(i).cargaActual = responsables.get(i).cargaActual - 1;
                }
            }
        }
        s.responsable = r.nombre;
        r.cargaActual = r.cargaActual + 1;
        guardar();
        System.out.println("Solicitud " + id + " asignada a " + r.nombre);
        return true;
    }


    public static void buscarPorEstado(String estado) {
        int n = 0;
        for (int i = 0; i < lista.size(); i++) {
            Solicitud s = lista.get(i);
            if (s.estado.equals(estado)) {
                System.out.println("#" + s.id + " | " + s.estudiante.nombre + " | " + s.tipo + " | " + s.prioridad
                        + " | " + s.estado + " | " + (s.responsable == null ? "sin asignar" : s.responsable));
                n++;
            }
        }
        System.out.println("Total encontradas: " + n);
    }

    public static void buscarPorTipo(String tipo) {
        int n = 0;
        for (int i = 0; i < lista.size(); i++) {
            Solicitud s = lista.get(i);
            if (s.tipo.equals(tipo)) {
                System.out.println("#" + s.id + " | " + s.estudiante.nombre + " | " + s.tipo + " | " + s.prioridad
                        + " | " + s.estado + " | " + (s.responsable == null ? "sin asignar" : s.responsable));
                n++;
            }
        }
        System.out.println("Total encontradas: " + n);
    }

    public static void buscarPorLegajo(String legajo) {
        int n = 0;
        for (int i = 0; i < lista.size(); i++) {
            Solicitud s = lista.get(i);
            if (s.estudiante.legajo.equals(legajo)) {
                System.out.println("#" + s.id + " | " + s.estudiante.nombre + " | " + s.tipo + " | " + s.prioridad
                        + " | " + s.estado + " | " + (s.responsable == null ? "sin asignar" : s.responsable));
                n++;
            }
        }
        System.out.println("Total encontradas: " + n);
    }

    // dias que tiene la oficina para resolver segun el tipo
    static int dias(String t) {
        if (t.equals("CERTIFICADO")) {
            return 5;
        } else if (t.equals("EQUIVALENCIA")) {
            return 15;
        } else if (t.equals("REINCORPORACION")) {
            return 10;
        } else {
            return 7;
        }
    }

    static String calcularVencimiento(Solicitud s) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate d = LocalDate.parse(s.fechaCreacion, f);
        return d.plusDays(dias(s.tipo)).format(f);
    }

    static boolean esUrgente(Solicitud s) {
        if ((s.prioridad.equals("ALTA") || s.tipo.equals("REINCORPORACION")) && !s.estado.equals("RESUELTA")
                && !s.estado.equals("CERRADA")) {
            return true;
        }
        return false;
    }

    public static void reporte() {
        System.out.println("===== REPORTE DE SOLICITUDES =====");
        int p = 0;
        int e = 0;
        int r = 0;
        int c = 0;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).estado.equals("PENDIENTE")) {
                p++;
            }
        }
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).estado.equals("EN_PROCESO")) {
                e++;
            }
        }
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).estado.equals("RESUELTA")) {
                r++;
            }
        }
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).estado.equals("CERRADA")) {
                c++;
            }
        }
        System.out.println("Total: " + lista.size());
        System.out.println("Pendientes: " + p);
        System.out.println("En proceso: " + e);
        System.out.println("Resueltas: " + r);
        System.out.println("Cerradas: " + c);
        System.out.println("--- Detalle ---");
        for (int i = 0; i < lista.size(); i++) {
            Solicitud s = lista.get(i);
            String urg = "NO";
            if (esUrgente(s)) {
                urg = "SI";
            }
            System.out.println("#" + s.id + " | " + s.estudiante.nombre + " (" + s.estudiante.legajo + ") | " + s.tipo
                    + " | " + s.prioridad + " | " + s.estado + " | " + (s.responsable == null ? "sin asignar" : s.responsable)
                    + " | vence " + calcularVencimiento(s) + " | urgente: " + urg);
        }
        System.out.println("--- Carga de responsables ---");
        for (int i = 0; i < responsables.size(); i++) {
            Responsable rr = responsables.get(i);
            System.out.println(rr.nombre + " (" + rr.area + "): " + rr.cargaActual + " solicitudes activas");
        }
    }

    static void imprimirDebug(Solicitud s) {
        System.out.println("DEBUG: " + s.id + " " + s.estado);
    }
}