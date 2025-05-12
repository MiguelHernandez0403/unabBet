package back_end;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Lugar {
    
    private String id;
    private String nombre;
    private String direccion;
    private String descripcion;
    private double calificacionPromedio;
    private List<Juego> juegosDisponibles;
    private List<Usuario> usuariosRegistrados;
    private List<Calificacion> calificaciones;
    
    public Lugar(String nombre, String direccion, String descripcion) {
        this.id = UUID.randomUUID().toString(); // Generación automática de ID único
        this.nombre = nombre;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.calificacionPromedio = 0.0;
        this.juegosDisponibles = new ArrayList<>();
        this.usuariosRegistrados = new ArrayList<>();
        this.calificaciones = new ArrayList<>();
    }
    
    public Lugar(String id, String nombre, String direccion, String descripcion, double calificacionPromedio) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.calificacionPromedio = calificacionPromedio;
        this.juegosDisponibles = new ArrayList<>();
        this.usuariosRegistrados = new ArrayList<>();
        this.calificaciones = new ArrayList<>();
    }
    
    public boolean crearLugar(String nombre, String direccion, String descripcion) {
        
        if (nombre == null || nombre.trim().isEmpty() ||
            direccion == null || direccion.trim().isEmpty()) {
            return false;
        }

        this.nombre = nombre;
        this.direccion = direccion;
        this.descripcion = descripcion != null ? descripcion : "";
                
        return true;
    }
    
    public boolean actualizarLugar(String nombre, String direccion, String descripcion) {
        boolean actualizado = false;
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre;
            actualizado = true;
        }
        
        if (direccion != null && !direccion.trim().isEmpty()) {
            this.direccion = direccion;
            actualizado = true;
        }
        
        if (descripcion != null) {
            this.descripcion = descripcion;
            actualizado = true;
        }
        
        return actualizado;
    }
    
    public boolean eliminarLugar() {
        
        for (Usuario usuario : usuariosRegistrados) {
            usuario.darseDeBajaEnLugar(this);
        }
        
        juegosDisponibles.clear();
        usuariosRegistrados.clear();
        calificaciones.clear();
        
        return true;
    }
    
    public Lugar consultarLugar() {
        return this;
    }
    
    public boolean agregarJuego(Juego juego) {
        if (juego != null && !juegosDisponibles.contains(juego)) {
            juegosDisponibles.add(juego);
            return true;
        }
        return false;
    }
    
    public boolean eliminarJuego(Juego juego) {
        if (juego != null && juegosDisponibles.contains(juego)) {
            juegosDisponibles.remove(juego);
            return true;
        }
        return false;
    }
    
    public boolean registrarUsuario(Usuario usuario) {
        if (usuario != null && !usuariosRegistrados.contains(usuario)) {
            usuariosRegistrados.add(usuario);
            return true;
        }
        return false;
    }
    
    public boolean quitarUsuario(Usuario usuario) {
        if (usuario != null && usuariosRegistrados.contains(usuario)) {
            usuariosRegistrados.remove(usuario);
            return true;
        }
        return false;
    }
    
    public boolean agregarCalificacion(Calificacion calificacion) {
        if (calificacion != null) {
            calificaciones.add(calificacion);
            actualizarCalificacionPromedio();
            return true;
        }
        return false;
    }
    
    private void actualizarCalificacionPromedio() {
        if (calificaciones.isEmpty()) {
            this.calificacionPromedio = 0.0;
            return;
        }
        
        double sumaCalificaciones = 0.0;
        for (Calificacion c : calificaciones) {
            sumaCalificaciones += c.getPuntuacion();
        }
        
        this.calificacionPromedio = sumaCalificaciones / calificaciones.size();
    }
    
    public double obtenerCalificacionPromedio() {
        return this.calificacionPromedio;
    }
    
    public List<Juego> obtenerJuegosDisponibles() {
        return new ArrayList<>(juegosDisponibles);
    }
    
    public List<Usuario> obtenerUsuariosRegistrados() {
        return new ArrayList<>(usuariosRegistrados);
    }
    
    public List<Calificacion> obtenerCalificaciones() {
        return new ArrayList<>(calificaciones);
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getCalificacionPromedio() {
        return calificacionPromedio;
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lugar lugar = (Lugar) o;
        return id.equals(lugar.id);
    }
    
    public int hashCode() {
        return Objects.hash(id);
    }
    
    public String toString() {
        return "Lugar{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", calificacionPromedio=" + calificacionPromedio +
                ", juegos disponibles=" + juegosDisponibles.size() +
                ", usuarios registrados=" + usuariosRegistrados.size() +
                '}';
    }
}
