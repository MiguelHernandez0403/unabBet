package back_end.Classes;

import back_end.Classes.Calificacion;
import back_end.Classes.Usuario;
import back_end.dao.LugarDAO;
import back_end.Excepciones.PersistenciaException;
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

    // Constructor para crear un nuevo lugar
    public Lugar(String nombre, String direccion, String descripcion) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.calificacionPromedio = 0.0;
        this.juegosDisponibles = new ArrayList<>();
        this.usuariosRegistrados = new ArrayList<>();
        this.calificaciones = new ArrayList<>();
    }

    // Constructor para cargar lugar existente desde JSON
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

    // Constructor sin parámetros para GSON
    public Lugar() {
        this.juegosDisponibles = new ArrayList<>();
        this.usuariosRegistrados = new ArrayList<>();
        this.calificaciones = new ArrayList<>();
    }

    /**
     * Crea un nuevo lugar y lo guarda en el archivo JSON
     */
    public boolean crearLugar(String nombre, String direccion, String descripcion) {

        if (nombre == null || nombre.trim().isEmpty()
                || direccion == null || direccion.trim().isEmpty()) {
            return false;
        }

        // Verificar si ya existe un lugar con el mismo nombre y dirección
        if (LugarDAO.existeLugar(nombre, direccion)) {
            System.err.println("Ya existe un lugar con el mismo nombre y dirección");
            return false;
        }

        this.nombre = nombre;
        this.direccion = direccion;
        this.descripcion = descripcion != null ? descripcion : "";

        try {
            // Guardar el lugar en el archivo JSON
            return LugarDAO.guardarLugar(this);
        } catch (PersistenciaException e) {
            System.err.println("Error al crear el lugar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza los datos del lugar en el archivo JSON
     */
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

        if (actualizado) {
            try {
                // Actualizar los datos en el archivo JSON
                return LugarDAO.actualizarLugar(this);
            } catch (PersistenciaException e) {
                System.err.println("Error al actualizar el lugar: " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    /**
     * Elimina el lugar del archivo JSON
     */
    public boolean eliminarLugar() {

        // Limpiar las listas en memoria
        for (Usuario usuario : usuariosRegistrados) {
            usuario.darseDeBajaEnLugar(this);
        }

        juegosDisponibles.clear();
        usuariosRegistrados.clear();
        calificaciones.clear();

        try {
            // Eliminar el lugar del archivo JSON
            return LugarDAO.eliminarLugar(this.id);
        } catch (PersistenciaException e) {
            System.err.println("Error al eliminar el lugar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Consulta y actualiza los datos del lugar desde el archivo JSON
     */
    public Lugar consultarLugar() {
        try {
            Lugar lugarActualizado = LugarDAO.buscarPorId(this.id);
            if (lugarActualizado != null) {
                // Actualizar los datos del objeto actual con los del archivo JSON
                this.nombre = lugarActualizado.getNombre();
                this.direccion = lugarActualizado.getDireccion();
                this.descripcion = lugarActualizado.getDescripcion();
                this.calificacionPromedio = lugarActualizado.getCalificacionPromedio();
            }
            return lugarActualizado != null ? lugarActualizado : this;
        } catch (PersistenciaException e) {
            System.err.println("Error al consultar el lugar: " + e.getMessage());
            return this;
        }
    }

    /**
     * Busca un lugar por ID en el archivo JSON
     */
    public static Lugar buscarPorId(String id) {
        try {
            return LugarDAO.buscarPorId(id);
        } catch (PersistenciaException e) {
            System.err.println("Error al buscar lugar por ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Busca lugares por nombre en el archivo JSON
     */
    public static List<Lugar> buscarPorNombre(String nombre) {
        try {
            return LugarDAO.buscarPorNombre(nombre);
        } catch (PersistenciaException e) {
            System.err.println("Error al buscar lugares por nombre: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene todos los lugares del archivo JSON
     */
    public static List<Lugar> obtenerTodosLosLugares() {
        try {
            return LugarDAO.obtenerTodosLosLugares();
        } catch (PersistenciaException e) {
            System.err.println("Error al obtener todos los lugares: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene lugares ordenados por calificación del archivo JSON
     */
    public static List<Lugar> obtenerLugaresPorCalificacion() {
        try {
            return LugarDAO.obtenerLugaresPorCalificacion();
        } catch (PersistenciaException e) {
            System.err.println("Error al obtener lugares por calificación: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Guarda los cambios actuales en el archivo JSON
     */
    public boolean guardarCambios() {
        try {
            return LugarDAO.actualizarLugar(this);
        } catch (PersistenciaException e) {
            System.err.println("Error al guardar cambios: " + e.getMessage());
            return false;
        }
    }

    // Métodos para manejar juegos
    public boolean agregarJuego(Juego juego) {
        if (juego != null && !juegosDisponibles.contains(juego)) {
            juegosDisponibles.add(juego);
            guardarCambios(); // Persistir cambios
            return true;
        }
        return false;
    }

    public boolean eliminarJuego(Juego juego) {
        if (juego != null && juegosDisponibles.contains(juego)) {
            juegosDisponibles.remove(juego);
            guardarCambios(); // Persistir cambios
            return true;
        }
        return false;
    }

    // Métodos para manejar usuarios
    public boolean registrarUsuario(Usuario usuario) {
        if (usuario != null && !usuariosRegistrados.contains(usuario)) {
            usuariosRegistrados.add(usuario);
            guardarCambios(); // Persistir cambios
            return true;
        }
        return false;
    }

    public boolean quitarUsuario(Usuario usuario) {
        if (usuario != null && usuariosRegistrados.contains(usuario)) {
            usuariosRegistrados.remove(usuario);
            guardarCambios(); // Persistir cambios
            return true;
        }
        return false;
    }

    // Métodos para manejar calificaciones
    public boolean agregarCalificacion(Calificacion calificacion) {
        if (calificacion != null) {
            calificaciones.add(calificacion);
            actualizarCalificacionPromedio();
            guardarCambios(); // Persistir cambios
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

    // Getters y setters
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

    public void setId(String id) {
        this.id = id;
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

    public void setCalificacionPromedio(double calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }

    public List<Juego> getJuegosDisponibles() {
        return juegosDisponibles;
    }

    public void setJuegosDisponibles(List<Juego> juegosDisponibles) {
        this.juegosDisponibles = juegosDisponibles;
    }

    public List<Usuario> getUsuariosRegistrados() {
        return usuariosRegistrados;
    }

    public void setUsuariosRegistrados(List<Usuario> usuariosRegistrados) {
        this.usuariosRegistrados = usuariosRegistrados;
    }

    public List<Calificacion> getCalificaciones() {
        return calificaciones;
    }

    public void setCalificaciones(List<Calificacion> calificaciones) {
        this.calificaciones = calificaciones;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Lugar lugar = (Lugar) o;
        return id.equals(lugar.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Lugar{"
                + "id='" + id + '\''
                + ", nombre='" + nombre + '\''
                + ", direccion='" + direccion + '\''
                + ", descripcion='" + descripcion + '\''
                + ", calificacionPromedio=" + calificacionPromedio
                + ", juegos disponibles=" + juegosDisponibles.size()
                + ", usuarios registrados=" + usuariosRegistrados.size()
                + '}';
    }
}
