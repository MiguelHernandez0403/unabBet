package back_end.dao;

import back_end.Classes.Usuario;
import back_end.Excepciones.PersistenciaException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private static final String ARCHIVO_JSON = "usuarios.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    // Clase interna para representar los datos del usuario en JSON
    private static class UsuarioJSON {
        private String id;
        private String uid;
        private String nombre;
        private String apellido;
        private String correo;
        private String contraseña;
        private String carrera;
        private int semestre;
        private double saldoAPUNAB;
        
        // Constructor vacío para Gson
        public UsuarioJSON() {}
        
        // Constructor a partir de Usuario
        public UsuarioJSON(Usuario usuario) {
            this.id = usuario.getId();
            this.uid = usuario.getuid();
            this.nombre = usuario.getNombre();
            this.apellido = usuario.getApellido();
            this.correo = usuario.getCorreo();
            this.contraseña = usuario.getContraseña();
            this.carrera = usuario.getCarrera();
            this.semestre = usuario.getSemestre();
            this.saldoAPUNAB = usuario.getSaldoAPUNAB();
        }
        
        // Método para convertir a Usuario
        public Usuario toUsuario() {
            return new Usuario(id, uid, nombre, apellido, correo, contraseña, carrera, semestre, saldoAPUNAB);
        }
        
        // Getters y setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        
        public String getContraseña() { return contraseña; }
        public void setContraseña(String contraseña) { this.contraseña = contraseña; }
        
        public String getCarrera() { return carrera; }
        public void setCarrera(String carrera) { this.carrera = carrera; }
        
        public int getSemestre() { return semestre; }
        public void setSemestre(int semestre) { this.semestre = semestre; }
        
        public double getSaldoAPUNAB() { return saldoAPUNAB; }
        public void setSaldoAPUNAB(double saldoAPUNAB) { this.saldoAPUNAB = saldoAPUNAB; }
    }
    
    /**
     * Inicializa el archivo JSON si no existe
     */
    private static void inicializarArchivo() throws PersistenciaException {
        try {
            Path archivoPath = Paths.get(ARCHIVO_JSON);
            
            // Si el archivo no existe, crear uno vacío con una lista vacía
            if (!Files.exists(archivoPath)) {
                List<UsuarioJSON> listaVacia = new ArrayList<>();
                String jsonVacio = gson.toJson(listaVacia);
                
                try (FileWriter writer = new FileWriter(ARCHIVO_JSON)) {
                    writer.write(jsonVacio);
                }
                
                System.out.println("Archivo " + ARCHIVO_JSON + " creado exitosamente.");
            }
        } catch (IOException e) {
            throw new PersistenciaException("Error al inicializar el archivo JSON: " + e.getMessage());
        }
    }
    
    /**
     * Lee todos los usuarios del archivo JSON
     */
    private static List<UsuarioJSON> leerUsuariosJSON() throws PersistenciaException {
        inicializarArchivo();
        
        try (FileReader reader = new FileReader(ARCHIVO_JSON)) {
            Type listType = new TypeToken<List<UsuarioJSON>>(){}.getType();
            List<UsuarioJSON> usuarios = gson.fromJson(reader, listType);
            
            // Si el archivo está vacío o contiene null, retornar lista vacía
            if (usuarios == null) {
                usuarios = new ArrayList<>();
            }
            
            return usuarios;
        } catch (IOException e) {
            throw new PersistenciaException("Error al leer el archivo JSON: " + e.getMessage());
        }
    }
    
    /**
     * Escribe todos los usuarios al archivo JSON
     */
    private static void escribirUsuariosJSON(List<UsuarioJSON> usuarios) throws PersistenciaException {
        try (FileWriter writer = new FileWriter(ARCHIVO_JSON)) {
            gson.toJson(usuarios, writer);
        } catch (IOException e) {
            throw new PersistenciaException("Error al escribir en el archivo JSON: " + e.getMessage());
        }
    }
    
    /**
     * Guarda un usuario en el archivo JSON
     */
    public static boolean guardarUsuario(Usuario usuario) throws PersistenciaException {
        if (usuario == null) {
            return false;
        }
        
        List<UsuarioJSON> usuarios = leerUsuariosJSON();
        UsuarioJSON usuarioJSON = new UsuarioJSON(usuario);
        
        // Verificar si el usuario ya existe (por ID)
        boolean existe = usuarios.stream()
                .anyMatch(u -> u.getId().equals(usuario.getId()));
        
        if (!existe) {
            usuarios.add(usuarioJSON);
            escribirUsuariosJSON(usuarios);
            return true;
        }
        
        return false; // Usuario ya existe
    }
    
    /**
     * Busca un usuario por su ID
     */
    public static Usuario buscarPorId(String id) throws PersistenciaException {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        
        List<UsuarioJSON> usuarios = leerUsuariosJSON();
        
        return usuarios.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .map(UsuarioJSON::toUsuario)
                .orElse(null);
    }
    
    /**
     * Busca un usuario por su correo electrónico
     */
    public static Usuario buscarPorCorreo(String correo) throws PersistenciaException {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }
        
        List<UsuarioJSON> usuarios = leerUsuariosJSON();
        
        return usuarios.stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase(correo.trim()))
                .findFirst()
                .map(UsuarioJSON::toUsuario)
                .orElse(null);
    }
    
    /**
     * Verifica si existe un correo en el sistema
     */
    public static boolean existeCorreo(String correo) throws PersistenciaException {
        return buscarPorCorreo(correo) != null;
    }
    
    /**
     * Obtiene todos los usuarios
     */
    public static List<Usuario> obtenerTodosLosUsuarios() throws PersistenciaException {
        List<UsuarioJSON> usuariosJSON = leerUsuariosJSON();
        List<Usuario> usuarios = new ArrayList<>();
        
        for (UsuarioJSON usuarioJSON : usuariosJSON) {
            usuarios.add(usuarioJSON.toUsuario());
        }
        
        return usuarios;
    }
    
    /**
     * Actualiza un usuario existente
     */
    public static boolean actualizarUsuario(Usuario usuario) throws PersistenciaException {
        if (usuario == null || usuario.getId() == null) {
            return false;
        }
        
        List<UsuarioJSON> usuarios = leerUsuariosJSON();
        
        // Buscar el usuario por ID y actualizarlo
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getId().equals(usuario.getId())) {
                usuarios.set(i, new UsuarioJSON(usuario));
                escribirUsuariosJSON(usuarios);
                return true;
            }
        }
        
        return false; // Usuario no encontrado
    }
    
    /**
     * Actualiza solo la contraseña de un usuario
     */
    public static boolean actualizarContraseña(String id, String nuevaContraseña) throws PersistenciaException {
        if (id == null || nuevaContraseña == null) {
            return false;
        }
        
        List<UsuarioJSON> usuarios = leerUsuariosJSON();
        
        // Buscar el usuario por ID y actualizar su contraseña
        for (UsuarioJSON usuario : usuarios) {
            if (usuario.getId().equals(id)) {
                usuario.setContraseña(nuevaContraseña);
                escribirUsuariosJSON(usuarios);
                return true;
            }
        }
        
        return false; // Usuario no encontrado
    }
    
    /**
     * Elimina un usuario del archivo JSON
     */
    public static boolean eliminarUsuario(String id) throws PersistenciaException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        List<UsuarioJSON> usuarios = leerUsuariosJSON();
        
        // Filtrar la lista para remover el usuario con el ID especificado
        int tamañoInicial = usuarios.size();
        usuarios.removeIf(u -> u.getId().equals(id));
        
        // Si se removió algún elemento, actualizar el archivo
        if (usuarios.size() < tamañoInicial) {
            escribirUsuariosJSON(usuarios);
            return true;
        }
        
        return false; // Usuario no encontrado
    }
    
    /**
     * Obtiene el número total de usuarios registrados
     */
    public static int contarUsuarios() throws PersistenciaException {
        List<UsuarioJSON> usuarios = leerUsuariosJSON();
        return usuarios.size();
    }

}//TODO: documentar en el readme
