package back_end.Classes;

public class SessionManager {

private static SessionManager instance;
    private Usuario usuarioActual;
    
    private SessionManager() {
        // Constructor privado para patrón Singleton
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }
    
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    public boolean hayUsuarioLogueado() {
        return usuarioActual != null;
    }
    
    public void cerrarSesion() {
        usuarioActual = null;
    }
    
    public String getNombreUsuario() {
        return usuarioActual != null ? 
            usuarioActual.getNombre() + " " + usuarioActual.getApellido() : 
            "Usuario no encontrado";
    }
    
}
