package back_end.dao;

import back_end.Classes.Juego;
import back_end.Excepciones.PersistenciaException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class JuegoDAO {
    
    private static final String ARCHIVO_JUEGOS = "juegos.json";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    // Serializador personalizado para LocalDateTime
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(FORMATTER));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), FORMATTER);
        }
    }
    
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    /**
     * Inicializa el archivo JSON si no existe
     */
    private static void inicializarArchivo() throws PersistenciaException {
        File archivo = new File(ARCHIVO_JUEGOS);
        if (!archivo.exists()) {
            try {
                // Crear el archivo con una lista vacía
                List<Juego> listaVacia = new ArrayList<>();
                try (FileWriter writer = new FileWriter(archivo)) {
                    gson.toJson(listaVacia, writer);
                }
            } catch (IOException e) {
                throw new PersistenciaException("Error al crear el archivo JSON de juegos: " + e.getMessage());
            }
        }
    }
    
    /**
     * Lee todos los juegos del archivo JSON
     */
    private static List<Juego> leerJuegos() throws PersistenciaException {
        inicializarArchivo();
        
        try (FileReader reader = new FileReader(ARCHIVO_JUEGOS)) {
            Type tipoLista = new TypeToken<List<Juego>>(){}.getType();
            List<Juego> juegos = gson.fromJson(reader, tipoLista);
            return juegos != null ? juegos : new ArrayList<>();
        } catch (IOException e) {
            throw new PersistenciaException("Error al leer el archivo JSON de juegos: " + e.getMessage());
        }
    }
    
    /**
     * Escribe todos los juegos al archivo JSON
     */
    private static void escribirJuegos(List<Juego> juegos) throws PersistenciaException {
        try (FileWriter writer = new FileWriter(ARCHIVO_JUEGOS)) {
            gson.toJson(juegos, writer);
        } catch (IOException e) {
            throw new PersistenciaException("Error al escribir el archivo JSON de juegos: " + e.getMessage());
        }
    }
    
    /**
     * Guarda un juego en el archivo JSON
     */
    public static boolean guardarJuego(Juego juego) throws PersistenciaException {
        if (juego == null) {
            return false;
        }
        
        List<Juego> juegos = leerJuegos();
        
        // Verificar si el juego ya existe por ID
        boolean existe = juegos.stream()
                .anyMatch(j -> j.getId().equals(juego.getId()));
        
        if (!existe) {
            juegos.add(juego);
            escribirJuegos(juegos);
            return true;
        }
        
        return false;
    }
    
    /**
     * Actualiza un juego existente en el archivo JSON
     */
    public static boolean actualizarJuego(Juego juego) throws PersistenciaException {
        if (juego == null) {
            return false;
        }
        
        List<Juego> juegos = leerJuegos();
        
        for (int i = 0; i < juegos.size(); i++) {
            if (juegos.get(i).getId().equals(juego.getId())) {
                juegos.set(i, juego);
                escribirJuegos(juegos);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Elimina un juego del archivo JSON por su ID (eliminación física)
     */
    public static boolean eliminarJuegoFisico(String id) throws PersistenciaException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        List<Juego> juegos = leerJuegos();
        boolean eliminado = juegos.removeIf(juego -> juego.getId().equals(id));
        
        if (eliminado) {
            escribirJuegos(juegos);
        }
        
        return eliminado;
    }
    
    /**
     * Busca un juego por su ID
     */
    public static Juego buscarPorId(String id) throws PersistenciaException {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        
        List<Juego> juegos = leerJuegos();
        return juegos.stream()
                .filter(juego -> juego.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Busca juegos por nombre (búsqueda parcial, case-insensitive)
     */
    public static List<Juego> buscarPorNombre(String nombre) throws PersistenciaException {
        if (nombre == null || nombre.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Juego> juegos = leerJuegos();
        return juegos.stream()
                .filter(juego -> juego.getNombre().toLowerCase()
                        .contains(nombre.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los juegos del archivo JSON
     */
    public static List<Juego> obtenerTodosLosJuegos() throws PersistenciaException {
        return new ArrayList<>(leerJuegos());
    }
    
    /**
     * Obtiene solo los juegos activos
     */
    public static List<Juego> obtenerJuegosActivos() throws PersistenciaException {
        List<Juego> juegos = leerJuegos();
        return juegos.stream()
                .filter(Juego::isActivo)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene juegos ordenados por factor multiplicador (de mayor a menor)
     */
    public static List<Juego> obtenerJuegosPorFactor() throws PersistenciaException {
        List<Juego> juegos = leerJuegos();
        return juegos.stream()
                .sorted(Comparator.comparingDouble(Juego::getFactorMultiplicador).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica si existe un juego con el nombre especificado
     */
    public static boolean existeJuego(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        
        try {
            List<Juego> juegos = leerJuegos();
            return juegos.stream()
                    .anyMatch(juego -> juego.getNombre().equalsIgnoreCase(nombre.trim()));
        } catch (PersistenciaException e) {
            System.err.println("Error al verificar existencia del juego: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cuenta el total de juegos
     */
    public static int contarJuegos() throws PersistenciaException {
        return leerJuegos().size();
    }
    
    /**
     * Cuenta solo los juegos activos
     */
    public static int contarJuegosActivos() throws PersistenciaException {
        List<Juego> juegos = leerJuegos();
        return (int) juegos.stream()
                .filter(Juego::isActivo)
                .count();
    }
    
    /**
     * Obtiene juegos por rango de factor multiplicador
     */
    public static List<Juego> buscarPorRangoFactor(double minimo, double maximo) 
            throws PersistenciaException {
        List<Juego> juegos = leerJuegos();
        return juegos.stream()
                .filter(juego -> juego.getFactorMultiplicador() >= minimo && 
                               juego.getFactorMultiplicador() <= maximo)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene juegos creados en un rango de fechas
     */
    public static List<Juego> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
            throws PersistenciaException {
        List<Juego> juegos = leerJuegos();
        return juegos.stream()
                .filter(juego -> {
                    LocalDateTime fechaCreacion = juego.getFechaCreacion();
                    return fechaCreacion.isAfter(fechaInicio) && fechaCreacion.isBefore(fechaFin);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene los juegos más recientes (ordenados por fecha de creación descendente)
     */
    public static List<Juego> obtenerJuegosRecientes(int limite) throws PersistenciaException {
        List<Juego> juegos = leerJuegos();
        return juegos.stream()
                .sorted(Comparator.comparing(Juego::getFechaCreacion).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene estadísticas básicas de los juegos
     */
    public static JuegoEstadisticas obtenerEstadisticas() throws PersistenciaException {
        List<Juego> juegos = leerJuegos();
        
        if (juegos.isEmpty()) {
            return new JuegoEstadisticas(0, 0, 0, 0.0, 0.0, 0.0);
        }
        
        int totalJuegos = juegos.size();
        int juegosActivos = (int) juegos.stream().filter(Juego::isActivo).count();
        int juegosInactivos = totalJuegos - juegosActivos;
        
        double factorPromedio = juegos.stream()
                .mapToDouble(Juego::getFactorMultiplicador)
                .average()
                .orElse(0.0);
        
        double factorMinimo = juegos.stream()
                .mapToDouble(Juego::getFactorMultiplicador)
                .min()
                .orElse(0.0);
        
        double factorMaximo = juegos.stream()
                .mapToDouble(Juego::getFactorMultiplicador)
                .max()
                .orElse(0.0);
        
        return new JuegoEstadisticas(totalJuegos, juegosActivos, juegosInactivos, 
                                   factorPromedio, factorMinimo, factorMaximo);
    }
    
    /**
     * Clase para encapsular estadísticas de juegos
     */
    public static class JuegoEstadisticas {
        private final int totalJuegos;
        private final int juegosActivos;
        private final int juegosInactivos;
        private final double factorPromedio;
        private final double factorMinimo;
        private final double factorMaximo;
        
        public JuegoEstadisticas(int totalJuegos, int juegosActivos, int juegosInactivos,
                               double factorPromedio, double factorMinimo, double factorMaximo) {
            this.totalJuegos = totalJuegos;
            this.juegosActivos = juegosActivos;
            this.juegosInactivos = juegosInactivos;
            this.factorPromedio = factorPromedio;
            this.factorMinimo = factorMinimo;
            this.factorMaximo = factorMaximo;
        }
        
        // Getters
        public int getTotalJuegos() { return totalJuegos; }
        public int getJuegosActivos() { return juegosActivos; }
        public int getJuegosInactivos() { return juegosInactivos; }
        public double getFactorPromedio() { return factorPromedio; }
        public double getFactorMinimo() { return factorMinimo; }
        public double getFactorMaximo() { return factorMaximo; }
        
        @Override
        public String toString() {
            return String.format(
                "Estadísticas de Juegos:\n" +
                "- Total: %d\n" +
                "- Activos: %d\n" +
                "- Inactivos: %d\n" +
                "- Factor promedio: %.2f\n" +
                "- Factor mínimo: %.2f\n" +
                "- Factor máximo: %.2f",
                totalJuegos, juegosActivos, juegosInactivos,
                factorPromedio, factorMinimo, factorMaximo
            );
        }
    }
    
}
