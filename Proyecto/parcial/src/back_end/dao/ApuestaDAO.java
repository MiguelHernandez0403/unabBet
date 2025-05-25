package back_end.dao;

import back_end.Classes.Apuesta;
import back_end.Classes.Usuario;
import back_end.Classes.Lugar;
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

public class ApuestaDAO {

    // Primero necesitamos crear la tabla de apuestas si no existe
    public static boolean inicializarTablaApuestas() {
        String sqlCrearTablaApuestas
                = "CREATE TABLE IF NOT EXISTS apuestas ("
                + "  id VARCHAR(50) PRIMARY KEY,"
                + "  estudiante_id VARCHAR(50) NOT NULL,"
                + "  lugar_id VARCHAR(50) NOT NULL,"
                + "  juego_id VARCHAR(50) NOT NULL,"
                + "  cantidad_apunab DOUBLE NOT NULL,"
                + "  fecha TIMESTAMP NOT NULL,"
                + "  ganada BOOLEAN DEFAULT FALSE,"
                + "  finalizada BOOLEAN DEFAULT FALSE,"
                + "  ganancia_potencial DOUBLE NOT NULL,"
                + "  ganancia_real DOUBLE DEFAULT 0,"
                + "  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "  FOREIGN KEY (estudiante_id) REFERENCES usuarios(id) ON DELETE CASCADE"
                + ")";

        String sqlCrearTablaApostadores
                = "CREATE TABLE IF NOT EXISTS apuesta_apostadores ("
                + "  apuesta_id VARCHAR(50) NOT NULL,"
                + "  usuario_id VARCHAR(50) NOT NULL,"
                + "  PRIMARY KEY (apuesta_id, usuario_id),"
                + "  FOREIGN KEY (apuesta_id) REFERENCES apuestas(id) ON DELETE CASCADE,"
                + "  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE"
                + ")";

        try (Connection conn = ConexionDB.obtenerConexion(); java.sql.Statement stmt = conn.createStatement()) {

            stmt.execute(sqlCrearTablaApuestas);
            stmt.execute(sqlCrearTablaApostadores);

            return true;
        } catch (SQLException e) {
            System.err.println("Error al inicializar tablas de apuestas: " + e.getMessage());
            return false;
        }
    }

    /**
     * CREATE - Guarda una nueva apuesta en la base de datos
     */
    public static boolean guardarApuesta(Apuesta apuesta) throws PersistenciaException {
        String sqlApuesta = "INSERT INTO apuestas (id, estudiante_id, lugar_id, juego_id, cantidad_apunab, "
                + "fecha, ganada, finalizada, ganancia_potencial, ganancia_real) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false); // Iniciar transacción

            // Insertar la apuesta principal
            try (PreparedStatement stmtApuesta = conn.prepareStatement(sqlApuesta)) {
                stmtApuesta.setString(1, apuesta.getId());
                stmtApuesta.setString(2, apuesta.getEstudiante().getId());
                stmtApuesta.setString(3, apuesta.getLugar().getId());
                stmtApuesta.setString(4, apuesta.getJuego().getId());
                stmtApuesta.setDouble(5, apuesta.getCantidadAPUNAB());
                stmtApuesta.setTimestamp(6, Timestamp.valueOf(apuesta.getFecha()));
                stmtApuesta.setBoolean(7, apuesta.isGanada());
                stmtApuesta.setBoolean(8, apuesta.isFinalizada());
                stmtApuesta.setDouble(9, apuesta.getGananciaPotencial());
                stmtApuesta.setDouble(10, apuesta.getGananciaReal());

                int filasAfectadas = stmtApuesta.executeUpdate();

                if (filasAfectadas > 0) {
                    // Guardar otros apostadores si existen
                    if (!apuesta.getOtrosApostadores().isEmpty()) {
                        guardarOtrosApostadores(conn, apuesta.getId(), apuesta.getOtrosApostadores());
                    }

                    conn.commit(); // Confirmar transacción
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            throw new PersistenciaException("Error al guardar la apuesta en la base de datos: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }

    /**
     * READ - Busca una apuesta por su ID
     */
    public static Apuesta buscarPorId(String id) throws PersistenciaException {
        String sql = "SELECT * FROM apuestas WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extraerApuestaDeResultSet(rs);
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar apuesta por ID: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * READ - Obtiene todas las apuestas de un usuario específico
     */
    public static List<Apuesta> buscarPorUsuario(String usuarioId) throws PersistenciaException {
        String sql = "SELECT * FROM apuestas WHERE estudiante_id = ? ORDER BY fecha DESC";
        List<Apuesta> apuestas = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                apuestas.add(extraerApuestaDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar apuestas por usuario: " + e.getMessage(), e);
        }

        return apuestas;
    }

    /**
     * READ - Obtiene todas las apuestas
     */
    public static List<Apuesta> obtenerTodasLasApuestas() throws PersistenciaException {
        String sql = "SELECT * FROM apuestas ORDER BY fecha DESC";
        List<Apuesta> apuestas = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                apuestas.add(extraerApuestaDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener todas las apuestas: " + e.getMessage(), e);
        }

        return apuestas;
    }

    /**
     * READ - Obtiene apuestas activas (no finalizadas)
     */
    public static List<Apuesta> obtenerApuestasActivas() throws PersistenciaException {
        String sql = "SELECT * FROM apuestas WHERE finalizada = FALSE ORDER BY fecha DESC";
        List<Apuesta> apuestas = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                apuestas.add(extraerApuestaDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener apuestas activas: " + e.getMessage(), e);
        }

        return apuestas;
    }

    /**
     * UPDATE - Actualiza una apuesta existente
     */
    public static boolean actualizarApuesta(Apuesta apuesta) throws PersistenciaException {
        String sqlApuesta = "UPDATE apuestas SET cantidad_apunab = ?, ganada = ?, finalizada = ?, "
                + "ganancia_potencial = ?, ganancia_real = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false);

            // Actualizar la apuesta principal
            try (PreparedStatement stmtApuesta = conn.prepareStatement(sqlApuesta)) {
                stmtApuesta.setDouble(1, apuesta.getCantidadAPUNAB());
                stmtApuesta.setBoolean(2, apuesta.isGanada());
                stmtApuesta.setBoolean(3, apuesta.isFinalizada());
                stmtApuesta.setDouble(4, apuesta.getGananciaPotencial());
                stmtApuesta.setDouble(5, apuesta.getGananciaReal());
                stmtApuesta.setString(6, apuesta.getId());

                int filasAfectadas = stmtApuesta.executeUpdate();

                if (filasAfectadas > 0) {
                    // Actualizar otros apostadores
                    eliminarOtrosApostadores(conn, apuesta.getId());
                    if (!apuesta.getOtrosApostadores().isEmpty()) {
                        guardarOtrosApostadores(conn, apuesta.getId(), apuesta.getOtrosApostadores());
                    }

                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            throw new PersistenciaException("Error al actualizar la apuesta: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }

    /**
     * UPDATE - Finaliza una apuesta (marca como ganada o perdida)
     */
    public static boolean finalizarApuesta(String apuestaId, boolean ganada, double gananciaReal) throws PersistenciaException {
        String sql = "UPDATE apuestas SET ganada = ?, finalizada = TRUE, ganancia_real = ? WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, ganada);
            stmt.setDouble(2, gananciaReal);
            stmt.setString(3, apuestaId);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al finalizar la apuesta: " + e.getMessage(), e);
        }
    }

    /**
     * DELETE - Elimina una apuesta de la base de datos
     */
    public static boolean eliminarApuesta(String id) throws PersistenciaException {
        String sql = "DELETE FROM apuestas WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar la apuesta: " + e.getMessage(), e);
        }
    }

    /**
     * DELETE - Elimina todas las apuestas de un usuario
     */
    public static boolean eliminarApuestasPorUsuario(String usuarioId) throws PersistenciaException {
        String sql = "DELETE FROM apuestas WHERE estudiante_id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuarioId);

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar apuestas del usuario: " + e.getMessage(), e);
        }
    }

    // Métodos auxiliares para manejar otros apostadores
    private static void guardarOtrosApostadores(Connection conn, String apuestaId, List<Usuario> apostadores) throws SQLException {
        String sql = "INSERT INTO apuesta_apostadores (apuesta_id, usuario_id) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Usuario apostador : apostadores) {
                stmt.setString(1, apuestaId);
                stmt.setString(2, apostador.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static void eliminarOtrosApostadores(Connection conn, String apuestaId) throws SQLException {
        String sql = "DELETE FROM apuesta_apostadores WHERE apuesta_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, apuestaId);
            stmt.executeUpdate();
        }
    }

    private static List<Usuario> obtenerOtrosApostadores(String apuestaId) throws SQLException {
        String sql = "SELECT u.* FROM usuarios u "
                + "INNER JOIN apuesta_apostadores aa ON u.id = aa.usuario_id "
                + "WHERE aa.apuesta_id = ?";

        List<Usuario> apostadores = new ArrayList<>();

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, apuestaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Usuario usuario = new Usuario(
                        rs.getString("id"),
                        rs.getString("uid"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("correo"),
                        rs.getString("contraseña"),
                        rs.getString("carrera"),
                        rs.getInt("semestre"),
                        rs.getDouble("saldo_apunab")
                );
                apostadores.add(usuario);
            }
        }

        return apostadores;
    }

    // Método auxiliar para extraer una apuesta del ResultSet
    private static Apuesta extraerApuestaDeResultSet(ResultSet rs) throws SQLException {
        try {
            String apuestaId = rs.getString("id");
            String estudianteId = rs.getString("estudiante_id");
            String lugarId = rs.getString("lugar_id");
            String juegoId = rs.getString("juego_id");

            // Obtener el estudiante, lugar y juego (esto requeriría implementar sus respectivos DAOs)
            Usuario estudiante = UsuarioDAO.buscarPorId(estudianteId);
            // Para lugar y juego necesitarías implementar LugarDAO y JuegoDAO
            // Por ahora crearemos objetos básicos o null
            Lugar lugar = null; // LugarDAO.buscarPorId(lugarId); 
            Juego juego = null; // JuegoDAO.buscarPorId(juegoId);

            // Obtener otros apostadores
            List<Usuario> otrosApostadores = obtenerOtrosApostadores(apuestaId);

            LocalDateTime fecha = rs.getTimestamp("fecha").toLocalDateTime();

            return new Apuesta(
                    apuestaId,
                    estudiante,
                    lugar,
                    juego,
                    rs.getDouble("cantidad_apunab"),
                    fecha,
                    otrosApostadores,
                    rs.getBoolean("ganada"),
                    rs.getBoolean("finalizada"),
                    rs.getDouble("ganancia_potencial"),
                    rs.getDouble("ganancia_real")
            );

        } catch (PersistenciaException e) {
            throw new SQLException("Error al extraer datos de usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Método utilitario para verificar si existe una apuesta
     */
    public static boolean existeApuesta(String id) {
        String sql = "SELECT COUNT(*) FROM apuestas WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de apuesta: " + e.getMessage());
        }

        return false;
    }

}//TODO: documentar en el readme
