package back_end.dao;


import back_end.Classes.Usuario;
import back_end.Excepciones.PersistenciaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class UsuarioDAO {
    
    public static boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE correo = ?";
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al verificar el correo en la base de datos: " + e.getMessage());
        }
        
        return false;
    }
    
    public static boolean guardarUsuario(Usuario usuario) throws PersistenciaException {
        String sql = "INSERT INTO usuarios (id, nombre, apellido, correo, contraseña, carrera, semestre, saldo_apunab) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getId());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getApellido());
            stmt.setString(4, usuario.getCorreo());
            stmt.setString(5, usuario.getContraseña());
            stmt.setString(6, usuario.getCarrera());
            stmt.setInt(7, usuario.getSemestre());
            stmt.setDouble(8, usuario.getSaldoAPUNAB());
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar el usuario en la base de datos: " + e.getMessage(), e);
        }
    }
    
    public static Usuario buscarPorId(String id) throws PersistenciaException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extraerUsuarioDeResultSet(rs);
            }
            
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar usuario por ID: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    public static Usuario buscarPorCorreo(String correo) throws PersistenciaException {
        String sql = "SELECT * FROM usuarios WHERE correo = ?";
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extraerUsuarioDeResultSet(rs);
            }
            
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar usuario por correo: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    public static boolean actualizarUsuario(Usuario usuario) throws PersistenciaException {
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, carrera = ?, " +
                     "semestre = ?, saldo_apunab = ? WHERE id = ?";
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getuid());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getApellido());
            stmt.setString(4, usuario.getCarrera());
            stmt.setInt(5, usuario.getSemestre());
            stmt.setDouble(6, usuario.getSaldoAPUNAB());
            stmt.setString(7, usuario.getId());
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar el usuario: " + e.getMessage(), e);
        }
    }
    
    public static boolean actualizarContraseña(String id, String nuevaContraseña) throws PersistenciaException {
        String sql = "UPDATE usuarios SET contraseña = ? WHERE id = ?";
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nuevaContraseña);
            stmt.setString(2, id);
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar la contraseña: " + e.getMessage(), e);
        }
    }
    
    public static boolean eliminarUsuario(String id) throws PersistenciaException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar el usuario: " + e.getMessage(), e);
        }
    }
    
    public static List<Usuario> obtenerTodosLosUsuarios() throws PersistenciaException {
        String sql = "SELECT * FROM usuarios";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(extraerUsuarioDeResultSet(rs));
            }
            
        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener todos los usuarios: " + e.getMessage(), e);
        }
        
        return usuarios;
    }
    
    private static Usuario extraerUsuarioDeResultSet(ResultSet rs) throws SQLException {
        return new Usuario(
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
    }
    
}//TODO: documentar en el readme
