package back_end.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    // Constantes de conexión - En una aplicación real, esto debería estar en un archivo de configuración
    private static final String URL = "jdbc:mysql://localhost:3306/apunab_db";
    private static final String USUARIO = "apunab_user";
    private static final String CONTRASEÑA = "apunab_password";

    // Flag para comprobar si se ha registrado el driver
    private static boolean driverRegistrado = false;

    public static Connection obtenerConexion() throws SQLException {
        // Registrar el driver de MySQL (solo se hace una vez)
        if (!driverRegistrado) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                driverRegistrado = true;
            } catch (ClassNotFoundException e) {
                throw new SQLException("No se encontró el driver de MySQL: " + e.getMessage());
            }
        }

        // Obtener la conexión
        return DriverManager.getConnection(URL, USUARIO, CONTRASEÑA);
    }

    public static boolean probarConexion() {
        try (Connection conn = obtenerConexion()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error al probar la conexión: " + e.getMessage());
            return false;
        }
    }

    public static void cerrarConexion(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    public static boolean inicializarBaseDeDatos() {
        String sqlCrearTablaUsuarios
                = "CREATE TABLE IF NOT EXISTS usuarios ("
                + "  id VARCHAR(50) PRIMARY KEY,"
                + "  uid VARCHAR(100) NOT NULL,"
                + "  nombre VARCHAR(100) NOT NULL,"
                + "  apellido VARCHAR(100) NOT NULL,"
                + "  correo VARCHAR(150) NOT NULL UNIQUE,"
                + "  contraseña VARCHAR(255) NOT NULL,"
                + "  carrera VARCHAR(100) NOT NULL,"
                + "  semestre INT NOT NULL,"
                + "  saldo_apunab DOUBLE DEFAULT 0,"
                + "  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";

        String sqlCrearTablaLugares
                = "CREATE TABLE IF NOT EXISTS lugares ("
                + "  id VARCHAR(50) PRIMARY KEY,"
                + "  nombre VARCHAR(100) NOT NULL,"
                + "  direccion VARCHAR(200) NOT NULL,"
                + "  descripcion TEXT,"
                + "  calificacion_promedio DOUBLE DEFAULT 0"
                + ")";

        String sqlCrearTablaJuegos
                = "CREATE TABLE IF NOT EXISTS juegos ("
                + "  id VARCHAR(50) PRIMARY KEY,"
                + "  nombre VARCHAR(100) NOT NULL,"
                + "  descripcion TEXT,"
                + "  factor_multiplicador DOUBLE NOT NULL DEFAULT 1.0,"
                + "  activo BOOLEAN DEFAULT TRUE,"
                + "  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";

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
                + "  FOREIGN KEY (estudiante_id) REFERENCES usuarios(id) ON DELETE CASCADE,"
                + "  FOREIGN KEY (lugar_id) REFERENCES lugares(id) ON DELETE CASCADE,"
                + "  FOREIGN KEY (juego_id) REFERENCES juegos(id) ON DELETE CASCADE"
                + ")";

        String sqlCrearTablaApostadores
                = "CREATE TABLE IF NOT EXISTS apuesta_apostadores ("
                + "  apuesta_id VARCHAR(50) NOT NULL,"
                + "  usuario_id VARCHAR(50) NOT NULL,"
                + "  fecha_union TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "  PRIMARY KEY (apuesta_id, usuario_id),"
                + "  FOREIGN KEY (apuesta_id) REFERENCES apuestas(id) ON DELETE CASCADE,"
                + "  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE"
                + ")";

        String sqlCrearTablaTransacciones
                = "CREATE TABLE IF NOT EXISTS transacciones ("
                + "  id VARCHAR(50) PRIMARY KEY,"
                + "  usuario_id VARCHAR(50) NOT NULL,"
                + "  tipo VARCHAR(50) NOT NULL,"
                + // 'APUESTA', 'GANANCIA', 'RECARGA', 'RETIRO'
                "  monto DOUBLE NOT NULL,"
                + "  descripcion TEXT,"
                + "  apuesta_id VARCHAR(50),"
                + "  fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,"
                + "  FOREIGN KEY (apuesta_id) REFERENCES apuestas(id) ON DELETE SET NULL"
                + ")";

        try (Connection conn = obtenerConexion(); java.sql.Statement stmt = conn.createStatement()) {

            // Crear tablas en orden debido a las dependencias de claves foráneas
            stmt.execute(sqlCrearTablaUsuarios);
            stmt.execute(sqlCrearTablaLugares);
            stmt.execute(sqlCrearTablaJuegos);
            stmt.execute(sqlCrearTablaApuestas);
            stmt.execute(sqlCrearTablaApostadores);
            stmt.execute(sqlCrearTablaTransacciones);

            System.out.println("Base de datos inicializada correctamente");
            return true;

        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            return false;
        }
    }

}//TODO: documentar en el readme
