package solicitudes;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        GestorSolicitudes.agregarResponsable("Ana Gomez", "Alumnado");
        GestorSolicitudes.agregarResponsable("Carlos Ruiz", "Academica");
        GestorSolicitudes.agregarResponsable("Laura Pena", "Mesa de Entradas");
        GestorSolicitudes.cargar();
        int op = -1;
        while (op != 0 && sc.hasNextLine()) {
            System.out.println();
            System.out.println("===== GESTION DE SOLICITUDES ACADEMICAS =====");
            System.out.println("1. Registrar solicitud");
            System.out.println("2. Cambiar estado");
            System.out.println("3. Asignar responsable");
            System.out.println("4. Buscar solicitudes");
            System.out.println("5. Reporte");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            try {
                op = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Opcion invalida");
                op = -1;
                continue;
            }
            if (op == 1) {
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
                GestorSolicitudes.registrarSolicitud(nombre, legajo, email, tipo, desc, prioridad, fecha);
            } else if (op == 2) {
                System.out.print("Id de la solicitud: ");
                int id = 0;
                try {
                    id = Integer.parseInt(sc.nextLine());
                } catch (Exception e) {
                    System.out.println("Id invalido");
                    continue;
                }
                System.out.print("Nuevo estado (PENDIENTE/EN_PROCESO/RESUELTA/CERRADA): ");
                String nuevo = sc.nextLine().toUpperCase();
                GestorSolicitudes.cambiarEstado(id, nuevo);
            } else if (op == 3) {
                System.out.print("Id de la solicitud: ");
                int id = 0;
                try {
                    id = Integer.parseInt(sc.nextLine());
                } catch (Exception e) {
                    System.out.println("Id invalido");
                    continue;
                }
                System.out.print("Nombre del responsable: ");
                String resp = sc.nextLine();
                GestorSolicitudes.asignarResponsable(id, resp);
            } else if (op == 4) {
                System.out.println("1. Por estado");
                System.out.println("2. Por tipo");
                System.out.println("3. Por legajo");
                System.out.print("Opcion: ");
                String sub = sc.nextLine();
                if (sub.equals("1")) {
                    System.out.print("Estado: ");
                    GestorSolicitudes.buscarPorEstado(sc.nextLine().toUpperCase());
                } else if (sub.equals("2")) {
                    System.out.print("Tipo: ");
                    GestorSolicitudes.buscarPorTipo(sc.nextLine().toUpperCase());
                } else if (sub.equals("3")) {
                    System.out.print("Legajo: ");
                    GestorSolicitudes.buscarPorLegajo(sc.nextLine());
                } else {
                    System.out.println("Opcion invalida");
                }
            } else if (op == 5) {
                GestorSolicitudes.reporte();
            } else if (op != 0) {
                System.out.println("Opcion invalida");
            }
        }
        System.out.println("Hasta luego");
    }
}