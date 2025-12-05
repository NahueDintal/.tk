import java.nio.file.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {

  public static void main(String[] args) {
    if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
      imprimirAyuda();
      return;
    }

    String directorioActual = System.getProperty("user.dir");
    String directorioTareas = Paths.get(directorioActual, ".tasker").toString();
    String archivoTareas = Paths.get(directorioTareas, "tasks.txt").toString();
    String mensjErrorId = "Error: Se requiere un ID";
    String mensjUso = "Uso: tasker <COMANDO> ";
    String comando = args[0].toLowerCase();

    try {
      switch (comando) {
        case "init":
          inicializarTasker(directorioTareas);
          break;
        case "add":
        case "a":
        case "new":
          agregarTarea(args, directorioTareas, archivoTareas);
          break;
        case "list":
        case "ls":
          listarTareas(archivoTareas);
          break;
        case "delete":
        case "del":
        case "rm":
          if (args.length < 2) {
            System.out.println(mensjErrorId);
            System.out.println(mensjUso);
            return;
          }
          eliminarTarea(args[1], archivoTareas);
          break;
        case "status":
          mostrarEstado(directorioTareas, archivoTareas);
          break;
        case "ok":
        case "c":
        case "complete":
          if (args.length < 2) {
            System.out.println(mensjErrorId);
            System.out.println(mensjUso);
            return;
          }
          completarTarea(args[1], archivoTareas);
          break;
        default:
          System.out.println("Comando no reconocido: " + comando);
          imprimirAyuda();
          break;
      }
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
  }

  public static void inicializarTasker(String directorioTareas) {
    Path path = Paths.get(directorioTareas);
    try {
      if (!Files.exists(path)) {
        Files.createDirectory(path);
        System.out.println("Tasker inicializado en: " + directorioTareas);
        System.out.println("Ahora puedes agregar tareas específicas de este proyecto.");
      } else {
        System.out.println("Tasker ya está inicializado en este directorio.");
      }
    } catch (IOException e) {
      System.err.println("Error al iniciar tasker: " + e.getMessage());
    } catch (InvalidPathException e) {
      System.err.println("La ruta del directorio es inválida: " + directorioTareas);
    }
  }

  static void agregarTarea(String[] args, String directorioTareas, String archivoTareas) {
    Path dirPath = Paths.get(directorioTareas);
    Path filePath = Paths.get(archivoTareas);

    try {
      if (!Files.exists(dirPath)) {
        System.out.println("Tasker no está inicializado en este directorio.");
        System.out.println("Ejecuta primero: tasker init");
        return;
      }

      String nombre = null;
      String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      String descripcion = "Sin descripción";
      String tipo = "Sin tipo";
      String prioridad = "media";

      for (int i = 1; i < args.length; i++) {
        switch (args[i]) {
          case "-n":
            if (i + 1 < args.length)
              nombre = args[++i];
            break;
          case "-f":
            if (i + 1 < args.length) {
              String fechaInput = args[++i];
              try {
                LocalDate.parse(fechaInput, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                fecha = fechaInput;
              } catch (DateTimeParseException e) {
                System.err.println("Formato de fecha inválido.");
                System.err.println("Esperando formato dd/MM/yyyy");
              }
            }
            break;
          case "-d":
            if (i + 1 < args.length)
              descripcion = args[++i];
            break;
          case "-t":
            if (i + 1 < args.length)
              tipo = args[++i];
            break;
          case "-p":
            if (i + 1 < args.length) {
              String p = args[++i].toLowerCase();
              if (p.equals("alta") || p.equals("media") || p.equals("baja")) {
                prioridad = p;
              } else {
                System.err.println("Prioridad inválida, usando 'media'.");
                System.err.println("Opciones válidas: alta, media, baja");
              }
            }
            break;
          default:
            System.out.println("Opción desconocida: " + args[i]);
            return;
        }
      }

      if (nombre == null || nombre.trim().isEmpty()) {
        System.err.println("Error: El nombre de la tarea es obligatorio (-n)");
        return;
      }

      // Crear archivo si no existe
      if (!Files.exists(filePath)) {
        Files.createFile(filePath);
        System.out.println("Archivo de tareas creado: " + archivoTareas);
      }

      // Obtener siguiente ID
      int siguienteId = obtenerSiguienteId(filePath);

      // Guardar tarea
      String lineaTarea = siguienteId + "|" + nombre + "|" + fecha + "|" +
          descripcion + "|" + tipo + "|" + prioridad;

      Files.write(filePath, (lineaTarea + System.lineSeparator()).getBytes(),
          StandardOpenOption.APPEND);

      String proyecto = Paths.get(System.getProperty("user.dir")).getFileName().toString();
      System.out.println("Tarea agregada con ID: " + siguienteId + " en " + proyecto);

    } catch (IOException e) {
      System.err.println("Error al agregar tarea: " + e.getMessage());
    }
  }

  static void listarTareas(String archivoTareas) {
    Path filePath = Paths.get(archivoTareas);

    try {
      if (!Files.exists(filePath)) {
        String proyecto = Paths.get(System.getProperty("user.dir")).getFileName().toString();
        System.out.println("No hay tareas guardadas en este proyecto (" + proyecto + ")");
        System.out.println("Para empezar: tasker init && tasker add -n \"Mi primera tarea\"");
        return;
      }

      String proyecto = Paths.get(System.getProperty("user.dir")).getFileName().toString();
      System.out.println("Tareas del proyecto: " + proyecto);
      System.out.println("-".repeat(50));

      List<String> lineas = Files.readAllLines(filePath, StandardCharsets.UTF_8);

      for (String linea : lineas) {
        String[] partes = linea.split("\\|");
        String estado = partes.length >= 7 ? partes[6] : "pendiente";
        String indicadorEstado = estado.equals("completada") ? "✅" : "⭕";

        if (partes.length >= 6) {
          System.out.println(String.format("%s %3s. %s",
              indicadorEstado, partes[0], partes[1]));
          System.out.println(String.format("     Fecha: %s | Tipo: %s | Prioridad: %s | Estado: %s",
              partes[2], partes[4], partes[5], estado));

          if (!partes[3].equals("Sin descripción")) {
            System.out.println("     Descripción: " + partes[3]);
          }
          System.out.println();
        }
      }

    } catch (IOException e) {
      System.err.println("Error al listar tareas: " + e.getMessage());
    }
  }

  static void eliminarTarea(String idAEliminar, String archivoTareas) {
    Path filePath = Paths.get(archivoTareas);

    try {
      if (!Files.exists(filePath)) {
        String proyecto = Paths.get(System.getProperty("user.dir")).getFileName().toString();
        System.out.println("No hay tareas guardadas en este proyecto (" + proyecto + ")");
        return;
      }

      List<String> lineas = Files.readAllLines(filePath, StandardCharsets.UTF_8);
      List<String> nuevasLineas = new ArrayList<>();
      boolean encontrada = false;

      for (String linea : lineas) {
        String[] partes = linea.split("\\|");
        if (partes.length >= 2 && partes[0].equals(idAEliminar)) {
          encontrada = true;
          System.out.println("Tarea eliminada: " + partes[0] + " - " + partes[1]);
        } else {
          nuevasLineas.add(linea);
        }
      }

      if (!encontrada) {
        System.out.println("No se encontró ninguna tarea con ID: " + idAEliminar);
        return;
      }

      Files.write(filePath, nuevasLineas, StandardCharsets.UTF_8);
      System.out.println("Tarea con ID " + idAEliminar + " eliminada exitosamente.");

    } catch (IOException e) {
      System.err.println("Error al eliminar tarea: " + e.getMessage());
    }
  }

  static void completarTarea(String idACompletar, String archivoTareas) {
    Path filePath = Paths.get(archivoTareas);

    try {
      if (!Files.exists(filePath)) {
        String proyecto = Paths.get(System.getProperty("user.dir")).getFileName().toString();
        System.out.println("No hay tareas guardadas en este proyecto (" + proyecto + ")");
        return;
      }

      List<String> lineas = Files.readAllLines(filePath, StandardCharsets.UTF_8);
      List<String> nuevasLineas = new ArrayList<>();
      boolean encontrada = false;

      for (String linea : lineas) {
        String[] partes = linea.split("\\|");
        if (partes.length >= 2 && partes[0].equals(idACompletar)) {
          encontrada = true;

          // Asegurar que el array tenga 7 elementos
          List<String> partesList = new ArrayList<>(Arrays.asList(partes));
          while (partesList.size() < 7) {
            partesList.add("pendiente");
          }
          partesList.set(6, "completada");

          partes = partesList.toArray(new String[0]);
          System.out.println("Tarea marcada como completada: " + partes[0] + " - " + partes[1]);
        }
        nuevasLineas.add(String.join("|", partes));
      }

      if (!encontrada) {
        System.out.println("No se encontró ninguna tarea con ID: " + idACompletar);
        return;
      }

      Files.write(filePath, nuevasLineas, StandardCharsets.UTF_8);
      System.out.println("Tarea con ID " + idACompletar + " marcada como completada.");

    } catch (IOException e) {
      System.err.println("Error al completar tarea: " + e.getMessage());
    }
  }

  static void mostrarEstado(String directorioTareas, String archivoTareas) {
    Path dirPath = Paths.get(directorioTareas);
    Path filePath = Paths.get(archivoTareas);

    boolean estaInicializado = Files.exists(dirPath);
    String proyecto = Paths.get(System.getProperty("user.dir")).getFileName().toString();

    System.out.println("Proyecto: " + proyecto);
    System.out.println("Tasker inicializado: " + (estaInicializado ? "SÍ" : "NO"));

    if (estaInicializado && Files.exists(filePath)) {
      try {
        List<String> lineas = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        int tareasTotales = lineas.size();
        int completadas = 0;

        for (String linea : lineas) {
          String[] partes = linea.split("\\|");
          String estado = partes.length >= 7 ? partes[6] : "pendiente";
          if (estado.equals("completada"))
            completadas++;
        }

        int pendientes = tareasTotales - completadas;

        System.out.println("Tareas totales: " + tareasTotales);
        System.out.println(" Pendientes: " + pendientes);
        System.out.println(" Completadas: " + completadas);

        if (tareasTotales > 0) {
          double porcentaje = (double) completadas / tareasTotales * 100;
          System.out.println(String.format(" Progreso: %.1f%%", porcentaje));
        }
      } catch (IOException e) {
        System.err.println("Error al leer tareas: " + e.getMessage());
      }
    } else if (estaInicializado) {
      System.out.println("Tareas: 0 (usa 'tasker add' para agregar la primera)");
    }
  }

  static int obtenerSiguienteId(Path filePath) throws IOException {
    if (!Files.exists(filePath) || Files.size(filePath) == 0) {
      return 1;
    }

    List<String> lineas = Files.readAllLines(filePath, StandardCharsets.UTF_8);
    int idMaximo = 0;

    for (String linea : lineas) {
      String[] partes = linea.split("\\|");
      if (partes.length > 0) {
        try {
          int id = Integer.parseInt(partes[0]);
          if (id > idMaximo)
            idMaximo = id;
        } catch (NumberFormatException e) {
          // Ignorar líneas mal formateadas
        }
      }
    }

    return idMaximo + 1;
  }

  public static void imprimirAyuda() {
    System.out.println("Tasker - Gestor de tareas por directorio.");
    System.out.println();
    System.out.println("USO:");
    System.out.println("  tasker <COMANDO> [OPCIONES]");
    System.out.println();
    System.out.println("COMANDOS:");
    System.out.println("  init                                Inicializar tasker en el directorio actual");
    System.out.println("  add, a, new                         Agregar una nueva tarea al direcotorio actual");
    System.out.println("  list, ls                            Listar tareas del directorio actual");
    System.out.println("  delete, del, rm                     Eliminar una tarea por ID");
    System.out.println("  complete, ok, c                     Completar tarea por ID");
    System.out.println("  status                              Mostrar estado en el directorio actual");
    System.out.println("  -h, --help                          Mostrar esta ayuda");
    System.out.println();
    System.out.println("OPCIONES PARA 'add':");
    System.out.println("  -n <nombre>                         Nombre de la tarea (OBLIGATORIO)");
    System.out.println("  -f <fecha>                          Fecha de la tarea");
    System.out.println("  -d <descripción>                    Descripción de la tarea");
    System.out.println("  -t <tipo>                           Tipo de tarea");
    System.out.println("  -p <prioridad>                      Prioridad (alta, media, baja)");
    System.out.println();
    System.out.println("FLUJO DE TRABAJO:");
    System.out.println("  1. tasker init                    # Inicializar en un proyecto");
    System.out.println("  2. tasker add -n \"Mi tarea\"       # Agregar tareas");
    System.out.println("  3. tasker list                    # Ver tareas del proyecto");
    System.out.println("  4. tasker complete                # Cambiar estado de la tarea a completado");
    System.out.println();
    System.out.println("EJEMPLOS:");
    System.out.println("  tasker init");
    System.out.println("  tasker add -n \"Implementar login\" -p alta");
    System.out.println("  tasker add -n \"Documentar API\" -t documentación");
    System.out.println("  tasker list");
    System.out.println("  tasker complete 2");
    System.out.println("  tasker delete 1");
    System.out.println("  tasker status");
  }
}
