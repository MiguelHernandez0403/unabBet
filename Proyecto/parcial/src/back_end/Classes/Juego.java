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
    
    public Juego(String nombre, String descripcion, double factorMultiplicador) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.factorMultiplicador = factorMultiplicador >= 1.0 ? factorMultiplicador : 1.0;
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }
    
    public Juego(String id, String nombre, String descripcion, double factorMultiplicador,boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.factorMultiplicador = factorMultiplicador >= 1.0 ? factorMultiplicador : 1.0;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
    }
    
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
            // Guardar el juego en la base de datos
            return JuegoDAO.guardarJuego(this);
        } catch (PersistenciaException e) {
            System.err.println("Error al crear el juego: " + e.getMessage());
            return false;
        }
    }
    
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
                // Actualizar los datos en la base de datos
                boolean actualizadoBD = JuegoDAO.actualizarJuego(this);
                if (!actualizadoBD) {
                    // Si no se pudo actualizar en BD, revertir cambios
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
    
    public Juego consultarJuego() {
        try {
            Juego juegoActualizado = JuegoDAO.buscarPorId(this.id);
            if (juegoActualizado != null) {
                // Actualizar los datos del objeto actual con los de la BD
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
    
    public static Juego buscarPorId(String id) {
        try {
            return JuegoDAO.buscarPorId(id);
        } catch (PersistenciaException e) {
            System.err.println("Error al buscar juego por ID: " + e.getMessage());
            return null;
        }
    }
    
    public static List<Juego> buscarPorNombre(String nombre) {
        try {
            return JuegoDAO.buscarPorNombre(nombre);
        } catch (PersistenciaException e) {
            System.err.println("Error al buscar juegos por nombre: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public static List<Juego> obtenerTodosLosJuegos() {
        try {
            return JuegoDAO.obtenerTodosLosJuegos();
        } catch (PersistenciaException e) {
            System.err.println("Error al obtener todos los juegos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public static List<Juego> obtenerJuegosActivos() {
        try {
            return JuegoDAO.obtenerJuegosActivos();
        } catch (PersistenciaException e) {
            System.err.println("Error al obtener juegos activos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public static List<Juego> obtenerJuegosPorFactor() {
        try {
            return JuegoDAO.obtenerJuegosPorFactor();
        } catch (PersistenciaException e) {
            System.err.println("Error al obtener juegos por factor: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public static boolean existeJuego(String nombre) {
        return JuegoDAO.existeJuego(nombre);
    }
    
    public static int contarJuegos() {
        try {
            return JuegoDAO.contarJuegos();
        } catch (PersistenciaException e) {
            System.err.println("Error al contar juegos: " + e.getMessage());
            return 0;
        }
    }
    
    public static int contarJuegosActivos() {
        try {
            return JuegoDAO.contarJuegosActivos();
        } catch (PersistenciaException e) {
            System.err.println("Error al contar juegos activos: " + e.getMessage());
            return 0;
        }
    }
    
    public String getId() {
        return id;
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
    
    public double calcularGananciaPotencial(double cantidadApuesta) {
        return cantidadApuesta * this.factorMultiplicador;
    }
    
    public String getEstadoTexto() {
        return activo ? "Activo" : "Inactivo";
    }
    
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
