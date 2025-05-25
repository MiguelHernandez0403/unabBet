package back_end.dao;

import back_end.Classes.Lugar;
import back_end.Excepciones.PersistenciaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class LugarDAO {

    /**
     * Verifica si ya existe un lugar con el mismo nombre y dirección
     */
    public static boolean existeLugar(String nombre, String direccion) {
        String sql = "SELECT COUNT(*) FROM lugares WHERE nombre = ? AND direccion = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, direccion);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar la existencia del lugar: " + e.getMessage());
        }

        return false;
    }

    /**
     * Guarda un nuevo lugar en la base de datos
     */
    public static boolean guardarLugar(Lugar lugar) throws PersistenciaException {
        String sql = "INSERT INTO lugares (id, nombre, direccion, descripcion, calificacion_promedio) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lugar.getId());
            stmt.setString(2, lugar.getNombre());
            stmt.setString(3, lugar.getDireccion());
            stmt.setString(4, lugar.getDescripcion());
            stmt.setDouble(5, lugar.getCalificacionPromedio());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar el lugar en la base de datos: " + e.getMessage(), e);
        }
    }

    /**
     * Busca un lugar por su ID
     */
    public static Lugar buscarPorId(String id) throws PersistenciaException {
        String sql = "SELECT * FROM lugares WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerLugarDeResultSet(rs);
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar lugar por ID: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Busca lugares por nombre (búsqueda parcial)
     */
    public static List<Lugar> buscarPorNombre(String nombre) throws PersistenciaException {
        String sql = "SELECT * FROM lugares WHERE nombre LIKE ?";
        List<Lugar> lugares = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nombre + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lugares.add(extraerLugarDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar lugares por nombre: " + e.getMessage(), e);
        }

        return lugares;
    }

    /**
     * Busca lugares por dirección (búsqueda parcial)
     */
    public static List<Lugar> buscarPorDireccion(String direccion) throws PersistenciaException {
        String sql = "SELECT * FROM lugares WHERE direccion LIKE ?";
        List<Lugar> lugares = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + direccion + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lugares.add(extraerLugarDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar lugares por dirección: " + e.getMessage(), e);
        }

        return lugares;
    }

    /**
     * Actualiza los datos de un lugar existente
     */
    public static boolean actualizarLugar(Lugar lugar) throws PersistenciaException {
        String sql = "UPDATE lugares SET nombre = ?, direccion = ?, descripcion = ?, "
                + "calificacion_promedio = ? WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lugar.getNombre());
            stmt.setString(2, lugar.getDireccion());
            stmt.setString(3, lugar.getDescripcion());
            stmt.setDouble(4, lugar.getCalificacionPromedio());
            stmt.setString(5, lugar.getId());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar el lugar: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza solo la calificación promedio de un lugar
     */
    public static boolean actualizarCalificacionPromedio(String id, double nuevaCalificacion) throws PersistenciaException {
        String sql = "UPDATE lugares SET calificacion_promedio = ? WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nuevaCalificacion);
            stmt.setString(2, id);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar la calificación promedio: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un lugar de la base de datos Nota: Las claves foráneas se
     * encargarán de eliminar registros relacionados
     */
    public static boolean eliminarLugar(String id) throws PersistenciaException {
        String sql = "DELETE FROM lugares WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar el lugar: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los lugares de la base de datos
     */
    public static List<Lugar> obtenerTodosLosLugares() throws PersistenciaException {
        String sql = "SELECT * FROM lugares ORDER BY nombre";
        List<Lugar> lugares = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lugares.add(extraerLugarDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener todos los lugares: " + e.getMessage(), e);
        }

        return lugares;
    }

    /**
     * Obtiene lugares ordenados por calificación (de mayor a menor)
     */
    public static List<Lugar> obtenerLugaresPorCalificacion() throws PersistenciaException {
        String sql = "SELECT * FROM lugares ORDER BY calificacion_promedio DESC";
        List<Lugar> lugares = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lugares.add(extraerLugarDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener lugares por calificación: " + e.getMessage(), e);
        }

        return lugares;
    }

    /**
     * Obtiene el número total de lugares registrados
     */
    public static int contarLugares() throws PersistenciaException {
        String sql = "SELECT COUNT(*) FROM lugares";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al contar lugares: " + e.getMessage(), e);
        }

        return 0;
    }

    /**
     * Extrae un objeto Lugar desde un ResultSet
     */
    private static Lugar extraerLugarDeResultSet(ResultSet rs) throws SQLException {
        return new Lugar(
                rs.getString("id"),
                rs.getString("nombre"),
                rs.getString("direccion"),
                rs.getString("descripcion"),
                rs.getDouble("calificacion_promedio")
        );
    }
}
