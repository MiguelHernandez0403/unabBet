package back_end.Classes;

import back_end.Classes.Lugar;
import back_end.Classes.Usuario;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Calificacion {

    private String id;
    private Usuario usuario;
    private Lugar lugar;
    private int puntuacion;
    private String comentario;
    private LocalDateTime fecha;

    public Calificacion(Usuario usuario, Lugar lugar, int puntuacion, String comentario) {
        this.id = UUID.randomUUID().toString();
        this.usuario = usuario;
        this.lugar = lugar;
        setPuntuacion(puntuacion);
        this.comentario = comentario != null ? comentario : "";
        this.fecha = LocalDateTime.now();
    }

    public Calificacion(String id, Usuario usuario, Lugar lugar, int puntuacion, String comentario, LocalDateTime fecha) {
        this.id = id;
        this.usuario = usuario;
        this.lugar = lugar;
        setPuntuacion(puntuacion);
        this.comentario = comentario != null ? comentario : "";
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
    }

    public boolean calificarLugar(Usuario usuario, Lugar lugar, int puntuacion, String comentario) {
        if (usuario == null || lugar == null) {
            return false;
        }

        this.usuario = usuario;
        this.lugar = lugar;
        setPuntuacion(puntuacion);
        this.comentario = comentario != null ? comentario : "";
        this.fecha = LocalDateTime.now();

        return lugar.agregarCalificacion(this);
    }

    public boolean actualizarCalificacion(int puntuacion, String comentario) {
        boolean actualizado = false;

        if (this.puntuacion != puntuacion && puntuacion >= 1 && puntuacion <= 5) {
            this.puntuacion = puntuacion;
            actualizado = true;
        }

        if (comentario != null) {
            this.comentario = comentario;
            actualizado = true;
        }

        if (actualizado) {
            this.fecha = LocalDateTime.now();

            if (this.lugar != null) {
                this.lugar.obtenerCalificacionPromedio();
            }
        }

        return actualizado;
    }

    public void setPuntuacion(int puntuacion) {
        if (puntuacion < 1) {
            this.puntuacion = 1;
        } else if (puntuacion > 5) {
            this.puntuacion = 5;
        } else {
            this.puntuacion = puntuacion;
        }
    }

    public String getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario != null ? comentario : "";
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getNombreUsuario() {
        if (usuario != null) {
            return usuario.getNombre() + " " + usuario.getApellido();
        }
        return "Usuario desconocido";
    }

    public String getNombreLugar() {
        if (lugar != null) {
            return lugar.getNombre();
        }
        return "Lugar desconocido";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Calificacion that = (Calificacion) o;
        return id.equals(that.id);
    }

    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString() {
        return "Calificacion{"
                + "id='" + id + '\''
                + ", usuario=" + (usuario != null ? usuario.getNombre() + " " + usuario.getApellido() : "null")
                + ", lugar=" + (lugar != null ? lugar.getNombre() : "null")
                + ", puntuacion=" + puntuacion
                + ", comentario='" + comentario + '\''
                + ", fecha=" + fecha
                + '}';
    }
}//TODO: documentar en el readme
