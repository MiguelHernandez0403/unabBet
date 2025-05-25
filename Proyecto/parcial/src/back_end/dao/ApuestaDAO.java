package back_end.dao;

import back_end.Classes.Apuesta;
import back_end.Classes.Usuario;
import back_end.Classes.Lugar;
import back_end.Classes.Juego;
import back_end.Excepciones.PersistenciaException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ApuestaDAO {

    private static final String ARCHIVO_APUESTAS = "apuestas.json";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /**
     * Inicializa el archivo JSON si no existe
     */
    public static boolean inicializarArchivoApuestas() throws PersistenciaException {
        File archivo = new File(ARCHIVO_APUESTAS);
        
        if (!archivo.exists()) {
            try {
                archivo.createNewFile();
                List<Apuesta> apuestasVacias = new ArrayList<>();
                guardarApuestasEnArchivo(apuestasVacias);
                System.out.println("Archivo " + ARCHIVO_APUESTAS + " creado exitosamente.");
                return true;
            } catch (IOException e) {
                System.err.println("Error al crear el archivo " + ARCHIVO_APUESTAS + ": " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * CREATE - Guarda una nueva apuesta en el archivo JSON
     */
    public static boolean guardarApuesta(Apuesta apuesta) throws PersistenciaException {
        try {
            List<Apuesta> apuestas = cargarApuestasDesdeArchivo();
            
            // Verificar que no exista ya una apuesta con el mismo ID
            boolean existe = apuestas.stream().anyMatch(a -> a.getId().equals(apuesta.getId()));
            if (existe) {
                throw new PersistenciaException("Ya existe una apuesta con el ID: " + apuesta.getId());
            }
            
            apuestas.add(apuesta);
            guardarApuestasEnArchivo(apuestas);
            return true;
            
        } catch (Exception e) {
            throw new PersistenciaException("Error al guardar la apuesta: " + e.getMessage(), e);
        }
    }

    /**
     * READ - Busca una apuesta por su ID
     */
    public static Apuesta buscarPorId(String id) throws PersistenciaException {
        try {
            List<Apuesta> apuestas = cargarApuestasDesdeArchivo();
            return apuestas.stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElse(null);
                    
        } catch (Exception e) {
            throw new PersistenciaException("Error al buscar apuesta por ID: " + e.getMessage(), e);
        }
    }

    /**
     * READ - Obtiene todas las apuestas de un usuario específico
     */
    public static List<Apuesta> buscarPorUsuario(String usuarioId) throws PersistenciaException {
        try {
            List<Apuesta> apuestas = cargarApuestasDesdeArchivo();
            return apuestas.stream()
                    .filter(a -> a.getEstudiante() != null && a.getEstudiante().getId().equals(usuarioId))
                    .sorted((a1, a2) -> a2.getFecha().compareTo(a1.getFecha()))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            throw new PersistenciaException("Error al buscar apuestas por usuario: " + e.getMessage(), e);
        }
    }

    /**
     * READ - Obtiene todas las apuestas
     */
    public static List<Apuesta> obtenerTodasLasApuestas() throws PersistenciaException {
        try {
            List<Apuesta> apuestas = cargarApuestasDesdeArchivo();
            return apuestas.stream()
                    .sorted((a1, a2) -> a2.getFecha().compareTo(a1.getFecha()))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            throw new PersistenciaException("Error al obtener todas las apuestas: " + e.getMessage(), e);
        }
    }

    /**
     * READ - Obtiene apuestas activas (no finalizadas)
     */
    public static List<Apuesta> obtenerApuestasActivas() throws PersistenciaException {
        try {
            List<Apuesta> apuestas = cargarApuestasDesdeArchivo();
            return apuestas.stream()
                    .filter(a -> !a.isFinalizada())
                    .sorted((a1, a2) -> a2.getFecha().compareTo(a1.getFecha()))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            throw new PersistenciaException("Error al obtener apuestas activas: " + e.getMessage(), e);
        }
    }

    /**
     * UPDATE - Actualiza una apuesta existente
     */
    public static boolean actualizarApuesta(Apuesta apuestaActualizada) throws PersistenciaException {
        try {
            List<Apuesta> apuestas = cargarApuestasDesdeArchivo();
            
            for (int i = 0; i < apuestas.size(); i++) {
                if (apuestas.get(i).getId().equals(apuestaActualizada.getId())) {
                    apuestas.set(i, apuestaActualizada);
                    guardarApuestasEnArchivo(apuestas);
                    return true;
                }
            }
            
            return false; // No se encontró la apuesta
            
        } catch (Exception e) {
            throw new PersistenciaException("Error al actualizar la apuesta: " + e.getMessage(), e);
        }
    }

    /**
     * UPDATE - Finaliza una apuesta (marca como ganada o perdida)
     */
    public static boolean finalizarApuesta(String apuestaId, boolean ganada, double gananciaReal) throws PersistenciaException {
        try {
            List<Apuesta> apuestas = cargarApuestasDesdeArchivo();
            
            for (Apuesta apuesta : apuestas) {
                if (apuesta.getId().equals(apuestaId)) {
                    // Como no podemos modificar directamente los campos privados,
                    // necesitamos crear una nueva instancia o usar métodos setter
                    // Por ahora, buscamos y actualizamos toda la apuesta
                    apuesta.finalizarApuesta(ganada);
                    guardarApuestasEnArchivo(apuestas);
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            throw new PersistenciaException("Error al finalizar la apuesta: " + e.getMessage(), e);
        }
    }

    /**
     * DELETE - Elimina una apuesta del archivo JSON
     */
    public static boolean eliminarApuesta(String id) throws PersistenciaException {
        try {
            List<Apuesta> apuestas = cargarApuestasDesdeArchivo();
            boolean eliminada = apuestas.removeIf(a -> a.getId().equals(id));
            
            if (eliminada) {
                guardarApuestasEnArchivo(apuestas);
            }
            
            return eliminada;
            
        } catch (Exception e) {
            throw new PersistenciaException("Error al eliminar la apuesta: " + e.getMessage(), e);
        }
    }

    /**
     * DELETE - Elimina todas las apuestas de un usuario
     */
    public static boolean eliminarApuestasPorUsuario(String usuarioId) throws PersistenciaException {
        try {
            List<Apuesta> apuestas = cargarApuestasDesdeArchivo();
            boolean eliminadas = apuestas.removeIf(a -> 
                a.getEstudiante() != null && a.getEstudiante().getId().equals(usuarioId)
            );
            
            if (eliminadas) {
                guardarApuestasEnArchivo(apuestas);
            }
            
            return eliminadas;
            
        } catch (Exception e) {
            throw new PersistenciaException("Error al eliminar apuestas del usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Método utilitario para verificar si existe una apuesta
     */
    public static boolean existeApuesta(String id) {
        try {
            List<Apuesta> apuestas = cargarApuestasDesdeArchivo();
            return apuestas.stream().anyMatch(a -> a.getId().equals(id));
        } catch (Exception e) {
            System.err.println("Error al verificar existencia de apuesta: " + e.getMessage());
            return false;
        }
    }

    // Métodos privados para manejo del archivo JSON
    
    /**
     * Carga todas las apuestas desde el archivo JSON
     */
    private static List<Apuesta> cargarApuestasDesdeArchivo() throws PersistenciaException {
        inicializarArchivoApuestas();
        
        try (FileReader reader = new FileReader(ARCHIVO_APUESTAS)) {
            Type listType = new TypeToken<List<Apuesta>>(){}.getType();
            List<Apuesta> apuestas = gson.fromJson(reader, listType);
            return apuestas != null ? apuestas : new ArrayList<>();
            
        } catch (JsonSyntaxException e) {
            // Si el archivo está corrupto o vacío, crear una lista vacía
            System.err.println("Archivo JSON corrupto, creando nueva lista: " + e.getMessage());
            return new ArrayList<>();
        } catch (IOException e) {
            throw new PersistenciaException("Error al leer el archivo de apuestas: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda todas las apuestas en el archivo JSON
     */
    private static void guardarApuestasEnArchivo(List<Apuesta> apuestas) throws PersistenciaException {
        try (FileWriter writer = new FileWriter(ARCHIVO_APUESTAS)) {
            gson.toJson(apuestas, writer);
            writer.flush();
            
        } catch (IOException e) {
            throw new PersistenciaException("Error al escribir el archivo de apuestas: " + e.getMessage(), e);
        }
    }

    // Clase auxiliar para serialización de LocalDateTime
    private static class LocalDateTimeAdapter implements com.google.gson.JsonSerializer<LocalDateTime>, com.google.gson.JsonDeserializer<LocalDateTime> {
        
        @Override
        public com.google.gson.JsonElement serialize(LocalDateTime dateTime, Type type, com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(dateTime.toString());
        }

        @Override
        public LocalDateTime deserialize(com.google.gson.JsonElement json, Type type, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
            return LocalDateTime.parse(json.getAsString());
        }
    }

}//TODO: documentar en el readme
