package back_end.dao;

import back_end.Classes.Juego;
import back_end.Excepciones.PersistenciaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class JuegoDAO {
    
    /**
     * Verifica si ya existe un juego con el mismo nombre
     */
    public static boolean existeJuego(String nombre) {
        String sql = "SELECT COUNT(*) FROM juegos WHERE nombre = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar la existencia del juego: " + e.getMessage());
        }

        return false;
    }

    /**
     * Guarda un nuevo juego en la base de datos
     */
    public static boolean guardarJuego(Juego juego) throws PersistenciaException {
        String sql = "INSERT INTO juegos (id, nombre, descripcion, factor_multiplicador, activo, fecha_creacion) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, juego.getId());
            stmt.setString(2, juego.getNombre());
            stmt.setString(3, juego.getDescripcion());
            stmt.setDouble(4, juego.getFactorMultiplicador());
            stmt.setBoolean(5, juego.isActivo());
            stmt.setTimestamp(6, Timestamp.valueOf(juego.getFechaCreacion()));

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar el juego en la base de datos: " + e.getMessage(), e);
        }
    }

    /**
     * Busca un juego por su ID
     */
    public static Juego buscarPorId(String id) throws PersistenciaException {
        String sql = "SELECT * FROM juegos WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerJuegoDeResultSet(rs);
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar juego por ID: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Busca juegos por nombre (búsqueda parcial)
     */
    public static List<Juego> buscarPorNombre(String nombre) throws PersistenciaException {
        String sql = "SELECT * FROM juegos WHERE nombre LIKE ? ORDER BY nombre";
        List<Juego> juegos = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nombre + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                juegos.add(extraerJuegoDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar juegos por nombre: " + e.getMessage(), e);
        }

        return juegos;
    }

    /**
     * Actualiza los datos de un juego existente
     */
    public static boolean actualizarJuego(Juego juego) throws PersistenciaException {
        String sql = "UPDATE juegos SET nombre = ?, descripcion = ?, factor_multiplicador = ?, "
                + "activo = ? WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, juego.getNombre());
            stmt.setString(2, juego.getDescripcion());
            stmt.setDouble(3, juego.getFactorMultiplicador());
            stmt.setBoolean(4, juego.isActivo());
            stmt.setString(5, juego.getId());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar el juego: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un juego de la base de datos (eliminación física)
     * Nota: Las claves foráneas se encargarán de manejar registros relacionados
     */
    public static boolean eliminarJuego(String id) throws PersistenciaException {
        String sql = "DELETE FROM juegos WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar el juego: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los juegos de la base de datos
     */
    public static List<Juego> obtenerTodosLosJuegos() throws PersistenciaException {
        String sql = "SELECT * FROM juegos ORDER BY nombre";
        List<Juego> juegos = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                juegos.add(extraerJuegoDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener todos los juegos: " + e.getMessage(), e);
        }

        return juegos;
    }

    /**
     * Obtiene solo los juegos activos
     */
    public static List<Juego> obtenerJuegosActivos() throws PersistenciaException {
        String sql = "SELECT * FROM juegos WHERE activo = TRUE ORDER BY nombre";
        List<Juego> juegos = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                juegos.add(extraerJuegoDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener juegos activos: " + e.getMessage(), e);
        }

        return juegos;
    }

    /**
     * Obtiene juegos ordenados por factor multiplicador (de mayor a menor)
     */
    public static List<Juego> obtenerJuegosPorFactor() throws PersistenciaException {
        String sql = "SELECT * FROM juegos ORDER BY factor_multiplicador DESC";
        List<Juego> juegos = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                juegos.add(extraerJuegoDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener juegos por factor: " + e.getMessage(), e);
        }

        return juegos;
    }

    /**
     * Busca juegos por rango de factor multiplicador
     */
    public static List<Juego> buscarPorRangoFactor(double factorMin, double factorMax) throws PersistenciaException {
        String sql = "SELECT * FROM juegos WHERE factor_multiplicador BETWEEN ? AND ? ORDER BY factor_multiplicador";
        List<Juego> juegos = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, factorMin);
            stmt.setDouble(2, factorMax);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                juegos.add(extraerJuegoDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar juegos por rango de factor: " + e.getMessage(), e);
        }

        return juegos;
    }

    /**
     * Actualiza solo el estado activo/inactivo de un juego
     */
    public static boolean actualizarEstadoJuego(String id, boolean activo) throws PersistenciaException {
        String sql = "UPDATE juegos SET activo = ? WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, activo);
            stmt.setString(2, id);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar el estado del juego: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza solo el factor multiplicador de un juego
     */
    public static boolean actualizarFactorMultiplicador(String id, double nuevoFactor) throws PersistenciaException {
        String sql = "UPDATE juegos SET factor_multiplicador = ? WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nuevoFactor);
            stmt.setString(2, id);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar el factor multiplicador: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el número total de juegos registrados
     */
    public static int contarJuegos() throws PersistenciaException {
        String sql = "SELECT COUNT(*) FROM juegos";

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al contar juegos: " + e.getMessage(), e);
        }

        return 0;
    }

    /**
     * Obtiene el número total de juegos activos
     */
    public static int contarJuegosActivos() throws PersistenciaException {
        String sql = "SELECT COUNT(*) FROM juegos WHERE activo = TRUE";

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al contar juegos activos: " + e.getMessage(), e);
        }

        return 0;
    }

    /**
     * Obtiene juegos creados en un período específico
     */
    public static List<Juego> obtenerJuegosPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws PersistenciaException {
        String sql = "SELECT * FROM juegos WHERE fecha_creacion BETWEEN ? AND ? ORDER BY fecha_creacion DESC";
        List<Juego> juegos = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                juegos.add(extraerJuegoDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener juegos por período: " + e.getMessage(), e);
        }

        return juegos;
    }

    /**
     * Obtiene el juego con el factor multiplicador más alto
     */
    public static Juego obtenerJuegoConMayorFactor() throws PersistenciaException {
        String sql = "SELECT * FROM juegos WHERE activo = TRUE ORDER BY factor_multiplicador DESC LIMIT 1";

        try (Connection conn = ConexionDB.obtenerConexion(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerJuegoDeResultSet(rs);
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener juego con mayor factor: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Extrae un objeto Juego desde un ResultSet
     */
    private static Juego extraerJuegoDeResultSet(ResultSet rs) throws SQLException {
        return new Juego(
                rs.getString("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDouble("factor_multiplicador"),
                rs.getBoolean("activo"),
                rs.getTimestamp("fecha_creacion").toLocalDateTime()
        );
    }
    
}
