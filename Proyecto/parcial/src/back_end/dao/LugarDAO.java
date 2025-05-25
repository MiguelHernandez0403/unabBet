package back_end.dao;

import back_end.Classes.Lugar;
import back_end.Classes.Juego;
import back_end.Classes.Usuario;
import back_end.Classes.Calificacion;
import back_end.Excepciones.PersistenciaException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LugarDAO {

    private static final String ARCHIVO_LUGARES = "lugares.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Inicializa el archivo JSON si no existe
     */
    private static void inicializarArchivo() throws PersistenciaException {
        File archivo = new File(ARCHIVO_LUGARES);
        if (!archivo.exists()) {
            try {
                // Crear el archivo con una lista vacía
                List<Lugar> listaVacia = new ArrayList<>();
                try (FileWriter writer = new FileWriter(archivo)) {
                    gson.toJson(listaVacia, writer);
                }
            } catch (IOException e) {
                throw new PersistenciaException("Error al crear el archivo JSON: " + e.getMessage());
            }
        }
    }
    
    /**
     * Lee todos los lugares del archivo JSON
     */
    private static List<Lugar> leerLugares() throws PersistenciaException {
        inicializarArchivo();
        
        try (FileReader reader = new FileReader(ARCHIVO_LUGARES)) {
            Type tipoLista = new TypeToken<List<Lugar>>(){}.getType();
            List<Lugar> lugares = gson.fromJson(reader, tipoLista);
            return lugares != null ? lugares : new ArrayList<>();
        } catch (IOException e) {
            throw new PersistenciaException("Error al leer el archivo JSON: " + e.getMessage());
        }
    }
    
    /**
     * Escribe todos los lugares al archivo JSON
     */
    private static void escribirLugares(List<Lugar> lugares) throws PersistenciaException {
        try (FileWriter writer = new FileWriter(ARCHIVO_LUGARES)) {
            gson.toJson(lugares, writer);
        } catch (IOException e) {
            throw new PersistenciaException("Error al escribir el archivo JSON: " + e.getMessage());
        }
    }
    
    /**
     * Guarda un lugar en el archivo JSON
     */
    public static boolean guardarLugar(Lugar lugar) throws PersistenciaException {
        if (lugar == null) {
            return false;
        }
        
        List<Lugar> lugares = leerLugares();
        
        // Verificar si el lugar ya existe
        boolean existe = lugares.stream()
                .anyMatch(l -> l.getId().equals(lugar.getId()));
        
        if (!existe) {
            lugares.add(lugar);
            escribirLugares(lugares);
            return true;
        }
        
        return false;
    }
    
    /**
     * Actualiza un lugar existente en el archivo JSON
     */
    public static boolean actualizarLugar(Lugar lugar) throws PersistenciaException {
        if (lugar == null) {
            return false;
        }
        
        List<Lugar> lugares = leerLugares();
        
        for (int i = 0; i < lugares.size(); i++) {
            if (lugares.get(i).getId().equals(lugar.getId())) {
                lugares.set(i, lugar);
                escribirLugares(lugares);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Elimina un lugar del archivo JSON por su ID
     */
    public static boolean eliminarLugar(String id) throws PersistenciaException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        List<Lugar> lugares = leerLugares();
        boolean eliminado = lugares.removeIf(lugar -> lugar.getId().equals(id));
        
        if (eliminado) {
            escribirLugares(lugares);
        }
        
        return eliminado;
    }
    
    /**
     * Busca un lugar por su ID
     */
    public static Lugar buscarPorId(String id) throws PersistenciaException {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        
        List<Lugar> lugares = leerLugares();
        return lugares.stream()
                .filter(lugar -> lugar.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Busca lugares por nombre (búsqueda parcial, case-insensitive)
     */
    public static List<Lugar> buscarPorNombre(String nombre) throws PersistenciaException {
        if (nombre == null || nombre.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Lugar> lugares = leerLugares();
        return lugares.stream()
                .filter(lugar -> lugar.getNombre().toLowerCase()
                        .contains(nombre.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los lugares del archivo JSON
     */
    public static List<Lugar> obtenerTodosLosLugares() throws PersistenciaException {
        return new ArrayList<>(leerLugares());
    }
    
    /**
     * Obtiene lugares ordenados por calificación (de mayor a menor)
     */
    public static List<Lugar> obtenerLugaresPorCalificacion() throws PersistenciaException {
        List<Lugar> lugares = leerLugares();
        return lugares.stream()
                .sorted((l1, l2) -> Double.compare(l2.getCalificacionPromedio(), 
                                                 l1.getCalificacionPromedio()))
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica si existe un lugar con el mismo nombre y dirección
     */
    public static boolean existeLugar(String nombre, String direccion) {
        if (nombre == null || direccion == null) {
            return false;
        }
        
        try {
            List<Lugar> lugares = leerLugares();
            return lugares.stream()
                    .anyMatch(lugar -> lugar.getNombre().equalsIgnoreCase(nombre.trim()) && 
                                     lugar.getDireccion().equalsIgnoreCase(direccion.trim()));
        } catch (PersistenciaException e) {
            System.err.println("Error al verificar existencia del lugar: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene la cantidad total de lugares
     */
    public static int contarLugares() throws PersistenciaException {
        return leerLugares().size();
    }
    
    /**
     * Busca lugares por rango de calificación
     */
    public static List<Lugar> buscarPorRangoCalificacion(double minimo, double maximo) 
            throws PersistenciaException {
        List<Lugar> lugares = leerLugares();
        return lugares.stream()
                .filter(lugar -> lugar.getCalificacionPromedio() >= minimo && 
                               lugar.getCalificacionPromedio() <= maximo)
                .collect(Collectors.toList());
    }
}
