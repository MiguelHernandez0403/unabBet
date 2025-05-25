package back_end.Classes;

import back_end.dao.JuegoDAO;
import back_end.Excepciones.PersistenciaException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Juego {
    
    private String id;
    private String nombre;
    private String descripcion;
    private double factorMultiplicador;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    
    // Constructor para crear un nuevo juego
    public Juego(String nombre, String descripcion, double factorMultiplicador) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.factorMultiplicador = factorMultiplicador >= 1.0 ? factorMultiplicador : 1.0;
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }
    
    // Constructor para cargar juego existente desde JSON
    public Juego(String id, String nombre, String descripcion, double factorMultiplicador, 
                 boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.factorMultiplicador = factorMultiplicador >= 1.0 ? factorMultiplicador : 1.0;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
    }
    
    // Constructor sin parámetros para GSON
    public Juego() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }
    
    /**
     * Crea un nuevo juego y lo guarda en el archivo JSON
     */
    public boolean crearJuego(String nombre, String descripcion, double factorMultiplicador) {
        // Validaciones básicas
        if (nombre == null || nombre.trim().isEmpty()) {
            System.err.println("El nombre del juego no puede estar vacío");
            return false;
        }

        if (factorMultiplicador < 1.0) {
            System.err.println("El factor multiplicador debe ser mayor o igual a 1.0");
            return false;
        }

        // Verificar si ya existe un juego con el mismo nombre
        if (JuegoDAO.existeJuego(nombre)) {
            System.err.println("Ya existe un juego con el mismo nombre");
            return false;
        }

        this.nombre = nombre.trim();
        this.descripcion = descripcion != null ? descripcion.trim() : "";
        this.factorMultiplicador = factorMultiplicador;
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();

        try {
            // Guardar el juego en el archivo JSON
            return JuegoDAO.guardarJuego(this);
        } catch (PersistenciaException e) {
            System.err.println("Error al crear el juego: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza los datos del juego en el archivo JSON
     */
    public boolean actualizarJuego(String nombre, String descripcion, double factorMultiplicador) {
        boolean actualizado = false;
        String nombreAnterior = this.nombre;
        String descripcionAnterior = this.descripcion;
        double factorAnterior = this.factorMultiplicador;

        if (nombre != null && !nombre.trim().isEmpty() && !nombre.equals(this.nombre)) {
            // Verificar que no exista otro juego con el mismo nombre
            if (!JuegoDAO.existeJuego(nombre)) {
                this.nombre = nombre.trim();
                actualizado = true;
            } else {
                System.err.println("Ya existe un juego con el nombre: " + nombre);
                return false;
            }
        }

        if (descripcion != null && !descripcion.equals(this.descripcion)) {
            this.descripcion = descripcion.trim();
            actualizado = true;
        }

        if (factorMultiplicador >= 1.0 && factorMultiplicador != this.factorMultiplicador) {
            this.factorMultiplicador = factorMultiplicador;
            actualizado = true;
        } else if (factorMultiplicador < 1.0) {
            System.err.println("El factor multiplicador debe ser mayor o igual a 1.0");
            return false;
        }

        if (actualizado) {
            try {
                // Actualizar los datos en el archivo JSON
                boolean actualizadoJSON = JuegoDAO.actualizarJuego(this);
                if (!actualizadoJSON) {
                    // Si no se pudo actualizar en JSON, revertir cambios
                    this.nombre = nombreAnterior;
                    this.descripcion = descripcionAnterior;
                    this.factorMultiplicador = factorAnterior;
                    return false;
                }
                return true;
            } catch (PersistenciaException e) {
                System.err.println("Error al actualizar el juego: " + e.getMessage());
                // Revertir cambios si hay error
                this.nombre = nombreAnterior;
                this.descripcion = descripcionAnterior;
                this.factorMultiplicador = factorAnterior;
                return false;
            }
        }

        return false;
    }
    
    /**
     * Elimina lógicamente el juego (marca como inactivo)
     */
    public boolean eliminarJuego() {
        try {
            // En lugar de eliminar físicamente, marcar como inactivo
            this.activo = false;
            boolean actualizado = JuegoDAO.actualizarJuego(this);
            
            if (!actualizado) {
                // Si no se pudo actualizar, revertir cambio
                this.activo = true;
                return false;
            }
            
            return true;
        } catch (PersistenciaException e) {
            System.err.println("Error al eliminar el juego: " + e.getMessage());
            this.activo = true; // Revertir cambio
            return false;
        }
    }
    
    /**
     * Elimina físicamente el juego del archivo JSON
     */
    public boolean eliminarJuegoCompleto() {
        try {
            return JuegoDAO.eliminarJuegoFisico(this.id);
        } catch (PersistenciaException e) {
            System.err.println("Error al eliminar completamente el juego: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reactiva un juego marcado como inactivo
     */
    public boolean reactivarJuego() {
        if (this.activo) {
            return true; // Ya está activo
        }

        try {
            this.activo = true;
            boolean actualizado = JuegoDAO.actualizarJuego(this);
            
            if (!actualizado) {
                this.activo = false;
                return false;
            }
            
            return true;
        } catch (PersistenciaException e) {
            System.err.println("Error al reactivar el juego: " + e.getMessage());
            this.activo = false;
            return false;
        }
    }
    
    /**
     * Consulta y actualiza los datos del juego desde el archivo JSON
     */
    public Juego consultarJuego() {
        try {
            Juego juegoActualizado = JuegoDAO.buscarPorId(this.id);
            if (juegoActualizado != null) {
                // Actualizar los datos del objeto actual con los del archivo JSON
                this.nombre = juegoActualizado.getNombre();
                this.descripcion = juegoActualizado.getDescripcion();
                this.factorMultiplicador = juegoActualizado.getFactorMultiplicador();
                this.activo = juegoActualizado.isActivo();
                this.fechaCreacion = juegoActualizado.getFechaCreacion();
                return juegoActualizado;
            }
        } catch (PersistenciaException e) {
            System.err.println("Error al consultar el juego: " + e.getMessage());
        }
        
        return this;
    }
    
    /**
     * Guarda los cambios actuales en el archivo JSON
     */
    public boolean guardarCambios() {
        try {
            return JuegoDAO.actualizarJuego(this);
        } catch (PersistenciaException e) {
            System.err.println("Error al guardar cambios: " + e.getMessage());
            return false;
        }
    }
    
    // ========== MÉTODOS ESTÁTICOS ==========
    
    /**
     * Busca un juego por ID en el archivo JSON
     */
    public static Juego buscarPorId(String id) {
        try {
            return JuegoDAO.buscarPorId(id);
        } catch (PersistenciaException e) {
            System.err.println("Error al buscar juego por ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Busca juegos por nombre en el archivo JSON
     */
    public static List<Juego> buscarPorNombre(String nombre) {
        try {
            return JuegoDAO.buscarPorNombre(nombre);
        } catch (PersistenciaException e) {
            System.err.println("Error al buscar juegos por nombre: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene todos los juegos del archivo JSON
     */
    public static List<Juego> obtenerTodosLosJuegos() {
        try {
            return JuegoDAO.obtenerTodosLosJuegos();
        } catch (PersistenciaException e) {
            System.err.println("Error al obtener todos los juegos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene solo los juegos activos del archivo JSON
     */
    public static List<Juego> obtenerJuegosActivos() {
        try {
            return JuegoDAO.obtenerJuegosActivos();
        } catch (PersistenciaException e) {
            System.err.println("Error al obtener juegos activos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene juegos ordenados por factor multiplicador
     */
    public static List<Juego> obtenerJuegosPorFactor() {
        try {
            return JuegoDAO.obtenerJuegosPorFactor();
        } catch (PersistenciaException e) {
            System.err.println("Error al obtener juegos por factor: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Verifica si existe un juego con el nombre especificado
     */
    public static boolean existeJuego(String nombre) {
        return JuegoDAO.existeJuego(nombre);
    }
    
    /**
     * Cuenta el total de juegos
     */
    public static int contarJuegos() {
        try {
            return JuegoDAO.contarJuegos();
        } catch (PersistenciaException e) {
            System.err.println("Error al contar juegos: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Cuenta solo los juegos activos
     */
    public static int contarJuegosActivos() {
        try {
            return JuegoDAO.contarJuegosActivos();
        } catch (PersistenciaException e) {
            System.err.println("Error al contar juegos activos: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Obtiene juegos por rango de factor multiplicador
     */
    public static List<Juego> buscarPorRangoFactor(double minimo, double maximo) {
        try {
            return JuegoDAO.buscarPorRangoFactor(minimo, maximo);
        } catch (PersistenciaException e) {
            System.err.println("Error al buscar juegos por rango de factor: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene los juegos más recientes
     */
    public static List<Juego> obtenerJuegosRecientes(int limite) {
        try {
            return JuegoDAO.obtenerJuegosRecientes(limite);
        } catch (PersistenciaException e) {
            System.err.println("Error al obtener juegos recientes: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene estadísticas de los juegos
     */
    public static JuegoDAO.JuegoEstadisticas obtenerEstadisticas() {
        try {
            return JuegoDAO.obtenerEstadisticas();
        } catch (PersistenciaException e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
            return null;
        }
    }
    
    // ========== MÉTODOS DE UTILIDAD ==========
    
    /**
     * Calcula la ganancia potencial para una cantidad de apuesta
     */
    public double calcularGananciaPotencial(double cantidadApuesta) {
        return cantidadApuesta * this.factorMultiplicador;
    }
    
    /**
     * Obtiene el estado del juego como texto
     */
    public String getEstadoTexto() {
        return activo ? "Activo" : "Inactivo";
    }
    
    // ========== GETTERS Y SETTERS ==========
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getFactorMultiplicador() {
        return factorMultiplicador;
    }

    public void setFactorMultiplicador(double factorMultiplicador) {
        this.factorMultiplicador = factorMultiplicador >= 1.0 ? factorMultiplicador : 1.0;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    // ========== MÉTODOS OBJECT ==========
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Juego juego = (Juego) o;
        return id.equals(juego.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Juego{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", factorMultiplicador=" + factorMultiplicador +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}//TODO: documentar en el readme
