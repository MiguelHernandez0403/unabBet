
package back_end;

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

    public Usuario(String uid,String nombre, String apellido, String correo, String contraseña, String carrera, int semestre) {
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

    public Usuario(String id,String uid, String nombre, String apellido, String correo, String contraseña, 
                  String carrera, int semestre, double saldoAPUNAB) {
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
    
    public boolean registrarse(String uid,String nombre, String apellido, String correo, String contraseña, String carrera, int semestre) {

        this.uid = uid;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contraseña = contraseña;
        this.carrera = carrera;
        this.semestre = semestre;
            
        return true;
    }
    
    public boolean iniciarSesion(String correo, String contraseña) {
        
        return this.correo.equals(correo) && this.contraseña.equals(contraseña);
    }
    
    public double obtenerSaldoAPUNAB() {
        return this.saldoAPUNAB;
    }
    
    public boolean actualizarPerfil(String uid, String nombre, String apellido, String carrera, int semestre) {
        if (uid != null && !uid.trim().isEmpty()) {
            this.nombre = nombre;
        }
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre;
        }
        if (apellido != null && !apellido.trim().isEmpty()) {
            this.apellido = apellido;
        }
        if (carrera != null && !carrera.trim().isEmpty()) {
            this.carrera = carrera;
        }
        if (semestre > 0) {
            this.semestre = semestre;
        }
        
        return true;
    }
    
    public boolean cambiarContraseña(String contraseñaActual, String nuevaContraseña) {
        if (this.contraseña.equals(contraseñaActual)) {
            this.contraseña = nuevaContraseña;
            return true;
        }
        return false;
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
        }
        return false;
    }
    
    public boolean darseDeBajaEnLugar(Lugar lugar) {
        if (lugar != null && lugaresRegistrados.contains(lugar)) {
            lugaresRegistrados.remove(lugar);
            lugar.quitarUsuario(this);
            return true;
        }
        return false;
    }
    
    public void agregarApuesta(Apuesta apuesta) {
        if (apuesta != null) {
            this.historialApuestas.add(apuesta);
        }
    }
    
    public boolean actualizarSaldo(double cantidad) {
        double nuevoSaldo = this.saldoAPUNAB + cantidad;
        if (nuevoSaldo >= 0) {
            this.saldoAPUNAB = nuevoSaldo;
            return true;
        }
        return false;
    }
    
    public List<Lugar> obtenerLugaresRegistrados() {
        return new ArrayList<>(lugaresRegistrados);
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ",uid='" + uid + "\'" +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", correo='" + correo + '\'' +
                ", carrera='" + carrera + '\'' +
                ", semestre=" + semestre +
                ", saldoAPUNAB=" + saldoAPUNAB +
                '}';
    }
}
