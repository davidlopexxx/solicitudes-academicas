package solicitudes;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        GestorSolicitudes gestor = new GestorSolicitudes(new RepositorioCsv("solicitudes.csv"));
        gestor.agregarResponsable("Ana Gomez", "Alumnado");
        gestor.agregarResponsable("Carlos Ruiz", "Academica");
        gestor.agregarResponsable("Laura Pena", "Mesa de Entradas");
        try {
            gestor.cargar();
        } catch (RuntimeException e) {
            System.out.println("Error al leer los datos guardados: " + e.getMessage());
            return;
        }
        ReporteSolicitudes reporte = new ReporteSolicitudes(gestor.getSolicitudes(), gestor.getResponsables());
        int op = -1;
        while (op != 0 && sc.hasNextLine()) {
            mostrarMenu();
            try {
                op = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Opcion invalida");
                op = -1;
                continue;
            }
            if (op == 1) {
                registrar(sc, gestor);
            } else if (op == 2) {
                cambiarEstado(sc, gestor);
            } else if (op == 3) {
                asignarResponsable(sc, gestor);
            } else if (op == 4) {
                buscar(sc, reporte);
            } else if (op == 5) {
                reporte.imprimirReporte();
            } else if (op != 0) {
                System.out.println("Opcion invalida");
            }
        }
        System.out.println("Hasta luego");
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("===== GESTION DE SOLICITUDES ACADEMICAS =====");
        System.out.println("1. Registrar solicitud");
        System.out.println("2. Cambiar estado");
        System.out.println("3. Asignar responsable");
        System.out.println("4. Buscar solicitudes");
        System.out.println("5. Reporte");
        System.out.println("0. Salir");
        System.out.print("Opcion: ");
    }

    // pide por teclado los datos de la solicitud y se los pasa al gestor
    private static void registrar(Scanner sc, GestorSolicitudes gestor) {
        System.out.print("Nombre del estudiante: ");
        String nombre = sc.nextLine();
        System.out.print("Legajo: ");
        String legajo = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Tipo (CERTIFICADO/EQUIVALENCIA/REINCORPORACION/OTRO): ");
        String tipo = sc.nextLine().toUpperCase();
        System.out.print("Descripcion: ");
        String desc = sc.nextLine();
        System.out.print("Prioridad (ALTA/MEDIA/BAJA): ");
        String prioridad = sc.nextLine().toUpperCase();
        System.out.print("Fecha (dd/MM/yyyy): ");
        String fecha = sc.nextLine();
        DatosSolicitud datos = new DatosSolicitud(nombre, legajo, email, tipo, desc, prioridad, fecha);
        gestor.registrarSolicitud(datos);
    }

    private static void cambiarEstado(Scanner sc, GestorSolicitudes gestor) {
        Integer id = leerId(sc);
        if (id == null) {
            return;
        }
        System.out.print("Nuevo estado (PENDIENTE/EN_PROCESO/RESUELTA/CERRADA): ");
        String nuevo = sc.nextLine().toUpperCase();
        gestor.cambiarEstado(id, nuevo);
    }

    private static void asignarResponsable(Scanner sc, GestorSolicitudes gestor) {
        Integer id = leerId(sc);
        if (id == null) {
            return;
        }
        System.out.print("Nombre del responsable: ");
        String resp = sc.nextLine();
        gestor.asignarResponsable(id, resp);
    }

    private static void buscar(Scanner sc, ReporteSolicitudes reporte) {
        System.out.println("1. Por estado");
        System.out.println("2. Por tipo");
        System.out.println("3. Por legajo");
        System.out.print("Opcion: ");
        String sub = sc.nextLine();
        if (sub.equals("1")) {
            System.out.print("Estado: ");
            reporte.buscarPorEstado(sc.nextLine().toUpperCase());
        } else if (sub.equals("2")) {
            System.out.print("Tipo: ");
            reporte.buscarPorTipo(sc.nextLine().toUpperCase());
        } else if (sub.equals("3")) {
            System.out.print("Legajo: ");
            reporte.buscarPorLegajo(sc.nextLine());
        } else {
            System.out.println("Opcion invalida");
        }
    }

    // pide el id de una solicitud; si lo escrito no es un numero avisa y devuelve null
    private static Integer leerId(Scanner sc) {
        System.out.print("Id de la solicitud: ");
        try {
            return Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Id invalido");
            return null;
        }
    }
}