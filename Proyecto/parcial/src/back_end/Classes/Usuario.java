package back_end.Classes;

import back_end.Excepciones.PersistenciaException;
import back_end.dao.UsuarioDAO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Usuario {

    private String id;
    private String uid;
    private String nombre;
    private String apellido;
    private String correo;
    private String contraseña;
    private String carrera;
    private int semestre;
    private double saldoAPUNAB;
    private List<Apuesta> historialApuestas;
    private List<Lugar> lugaresRegistrados;

    public Usuario(String uid, String nombre, String apellido, String correo, String contraseña, String carrera, int semestre) {
        this.id = UUID.randomUUID().toString();
        this.uid = uid;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contraseña = contraseña;
        this.carrera = carrera;
        this.semestre = semestre;
        this.saldoAPUNAB = 0.0;
        this.historialApuestas = new ArrayList<>();
        this.lugaresRegistrados = new ArrayList<>();
    }

    public Usuario(String id, String uid, String nombre, String apellido, String correo, String contraseña, String carrera, int semestre, double saldoAPUNAB) {
        this.id = id;
        this.uid = uid;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contraseña = contraseña;
        this.carrera = carrera;
        this.semestre = semestre;
        this.saldoAPUNAB = saldoAPUNAB;
        this.historialApuestas = new ArrayList<>();
        this.lugaresRegistrados = new ArrayList<>();
    }

    public boolean registrarse(String nombre, String apellido, String correo, String contraseña, String carrera, int semestre)
            throws IllegalArgumentException, PersistenciaException {

        // Validación de datos de entrada
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("El id universitario no puede estar vacío");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }

        // Validación del correo electrónico
        if (correo == null || !validarFormatoCorreo(correo)) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido");
        }

        // Verificar que sea un correo institucional (ejemplo: termina en @unab.edu.co)
        if (!correo.toLowerCase().endsWith("@unab.edu.co")) {
            throw new IllegalArgumentException("Debe utilizar un correo institucional (@unab.edu.co)");
        }

        // Verificar que el correo no exista ya en el sistema
        if (UsuarioDAO.existeCorreo(correo)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado en el sistema");
        }

        // Validación de la contraseña
        if (contraseña == null || contraseña.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        if (!validarFormatoContraseña(contraseña)) {
            throw new IllegalArgumentException("La contraseña debe contener al menos una letra mayúscula, una minúscula y un número");
        }

        // Validación de la carrera
        if (carrera == null || carrera.trim().isEmpty()) {
            throw new IllegalArgumentException("La carrera no puede estar vacía");
        }

        // Validación del semestre
        if (semestre <= 0 || semestre > 10) {
            throw new IllegalArgumentException("El semestre debe estar entre 1 y 12");
        }

        // Si todas las validaciones son exitosas, actualizar los datos
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
        this.correo = correo.trim().toLowerCase();
        this.contraseña = cifrarContraseña(contraseña); // Cifrar la contraseña antes de guardarla
        this.carrera = carrera.trim();
        this.semestre = semestre;

        // Guardar el usuario en la base de datos
        try {
            return UsuarioDAO.guardarUsuario(this);
        } catch (Exception e) {
            throw new PersistenciaException("Error al guardar el usuario en la base de datos: " + e.getMessage());
        }
    }

    private boolean validarFormatoCorreo(String correo) {
        // Patrón básico para validación de correo electrónico
        String patronCorreo = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return correo.matches(patronCorreo);
    }

    private boolean validarFormatoContraseña(String contraseña) {
        // Verifica que la contraseña tenga al menos una letra mayúscula, una minúscula y un número
        boolean tieneMayuscula = false;
        boolean tieneMinuscula = false;
        boolean tieneNumero = false;

        for (char c : contraseña.toCharArray()) {
            if (Character.isUpperCase(c)) {
                tieneMayuscula = true;
            } else if (Character.isLowerCase(c)) {
                tieneMinuscula = true;
            } else if (Character.isDigit(c)) {
                tieneNumero = true;
            }
        }

        return tieneMayuscula && tieneMinuscula && tieneNumero;
    }

    private String cifrarContraseña(String contraseña) {
        // En una implementación real, usaríamos una librería de cifrado segura

        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(contraseña.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Convertir el hash a hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // En caso de error, devolvemos una versión menos segura (solo para este ejemplo)
            System.err.println("Error al cifrar la contraseña: " + e.getMessage());
            return contraseña + "salt_example";
        }
    }

    public static Usuario iniciarSesion(String correo, String contraseña) throws PersistenciaException {
        // Buscar el usuario por correo
        Usuario usuario = UsuarioDAO.buscarPorCorreo(correo);

        if (usuario == null) {
            return null; // Usuario no encontrado
        }

        // Cifrar la contraseña ingresada para compararla con la almacenada
        String contraseñaCifrada = usuario.cifrarContraseña(contraseña);

        // Verificar si las contraseñas coinciden
        if (usuario.getContraseña().equals(contraseñaCifrada)) {

            return usuario;
        }

        return null; // Contraseña incorrecta
    }

    public static Usuario buscarPorId(String id) throws PersistenciaException {
        return UsuarioDAO.buscarPorId(id);
    }

    public static Usuario buscarPorCorreo(String correo) throws PersistenciaException {
        return UsuarioDAO.buscarPorCorreo(correo);
    }

    public static List<Usuario> obtenerTodos() throws PersistenciaException {
        return UsuarioDAO.obtenerTodosLosUsuarios();
    }

    public double obtenerSaldoAPUNAB() {
        return this.saldoAPUNAB;
    }

    public boolean actualizarPerfil(String nombre, String apellido, String carrera, int semestre)
            throws PersistenciaException {
        // Actualizar atributos en memoria
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre.trim();
        }
        if (apellido != null && !apellido.trim().isEmpty()) {
            this.apellido = apellido.trim();
        }
        if (carrera != null && !carrera.trim().isEmpty()) {
            this.carrera = carrera.trim();
        }
        if (semestre > 0 && semestre <= 12) {
            this.semestre = semestre;
        }

        // Actualizar en la base de datos
        return UsuarioDAO.actualizarUsuario(this);
    }

    public boolean cambiarContraseña(String contraseñaActual, String nuevaContraseña)
            throws PersistenciaException {
        // Verificar la contraseña actual
        String contraseñaActualCifrada = cifrarContraseña(contraseñaActual);

        if (!this.contraseña.equals(contraseñaActualCifrada)) {
            return false; // La contraseña actual es incorrecta
        }

        // Validar formato de la nueva contraseña
        if (nuevaContraseña == null || nuevaContraseña.length() < 8 || !validarFormatoContraseña(nuevaContraseña)) {
            return false; // La nueva contraseña no cumple con los requisitos
        }

        // Cifrar la nueva contraseña
        String nuevaContraseñaCifrada = cifrarContraseña(nuevaContraseña);

        // Actualizar en memoria
        this.contraseña = nuevaContraseñaCifrada;

        // Actualizar en la base de datos
        return UsuarioDAO.actualizarContraseña(this.id, nuevaContraseñaCifrada);
    }

    public List<Apuesta> obtenerHistorialApuestas() {
        return new ArrayList<>(historialApuestas);
    }

    public double calcularAPUNABFaltantes() {
        final double APUNAB_REQUERIDAS = 100000.0;
        double faltantes = APUNAB_REQUERIDAS - this.saldoAPUNAB;
        return faltantes > 0 ? faltantes : 0;
    }

    public boolean registrarseEnLugar(Lugar lugar) {
        if (lugar != null && !lugaresRegistrados.contains(lugar)) {
            lugaresRegistrados.add(lugar);
            lugar.registrarUsuario(this);
            return true;
            //Aqui se podri llamar un metodo que guarde en la BD
        }
        return false;
    }

    public boolean darseDeBajaEnLugar(Lugar lugar) {
        if (lugar != null && lugaresRegistrados.contains(lugar)) {
            lugaresRegistrados.remove(lugar);
            lugar.quitarUsuario(this);
            return true;
            //Aqui se podri llamar un metodo que guarde los cambios en la BD
        }
        return false;
    }

    public void agregarApuesta(Apuesta apuesta) {
        if (apuesta != null) {
            this.historialApuestas.add(apuesta);
            //Aqui se podri llamar un metodo que guarde en la BD
        }
    }

    public boolean actualizarSaldo(double cantidad) throws PersistenciaException {
        double nuevoSaldo = this.saldoAPUNAB + cantidad;

        if (nuevoSaldo >= 0) {
            // Actualizar en memoria
            this.saldoAPUNAB = nuevoSaldo;

            // Actualizar en la base de datos
            return UsuarioDAO.actualizarUsuario(this);
        }

        return false; // No se puede tener saldo negativo
    }

    public List<Lugar> obtenerLugaresRegistrados() {
        return new ArrayList<>(lugaresRegistrados);
    }

    public boolean eliminar() throws PersistenciaException {
        // Eliminar el usuario de la base de datos
        return UsuarioDAO.eliminarUsuario(this.id);
    }

    public String getId() {
        return id;
    }

    public String getuid() {
        return uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getContraseña() {
        return contraseña;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public double getSaldoAPUNAB() {
        return saldoAPUNAB;
    }

    public void setSaldoAPUNAB(double saldoAPUNAB) {
        this.saldoAPUNAB = saldoAPUNAB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Usuario usuario = (Usuario) o;
        return id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{"
                + "id='" + id + '\''
                + ",uid='" + uid + "\'"
                + ", nombre='" + nombre + '\''
                + ", apellido='" + apellido + '\''
                + ", correo='" + correo + '\''
                + ", carrera='" + carrera + '\''
                + ", semestre=" + semestre
                + ", saldoAPUNAB=" + saldoAPUNAB
                + '}';
    }
}//TODO: documentar en el readme
