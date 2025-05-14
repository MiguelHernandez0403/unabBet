package back_end.Classes;

import back_end.Classes.Lugar;
import back_end.Classes.Usuario;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Apuesta {
    
    private String id;
    private Usuario estudiante;
    private Lugar lugar;
    private Juego juego;
    private double cantidadAPUNAB;
    private LocalDateTime fecha;
    private List<Usuario> otrosApostadores;
    private boolean ganada;
    private boolean finalizada;
    private double gananciaPotencial;
    private double gananciaReal;
    
    public Apuesta(Usuario estudiante, Lugar lugar, Juego juego, double cantidadAPUNAB) {
        this.id = UUID.randomUUID().toString();
        this.estudiante = estudiante;
        this.lugar = lugar;
        this.juego = juego;
        this.cantidadAPUNAB = cantidadAPUNAB > 0 ? cantidadAPUNAB : 0;
        this.fecha = LocalDateTime.now();
        this.otrosApostadores = new ArrayList<>();
        this.ganada = false;
        this.finalizada = false;
        this.gananciaPotencial = calcularGananciaPotencial();
        this.gananciaReal = 0.0;
    }
    
    public Apuesta(Usuario estudiante, Lugar lugar, Juego juego, double cantidadAPUNAB, List<Usuario> otrosApostadores) {
        this(estudiante, lugar, juego, cantidadAPUNAB);
        if (otrosApostadores != null) {
            this.otrosApostadores.addAll(otrosApostadores);
        }
    }
    
    public Apuesta(String id, Usuario estudiante, Lugar lugar, Juego juego, double cantidadAPUNAB, LocalDateTime fecha, List<Usuario> otrosApostadores, boolean ganada, boolean finalizada, double gananciaPotencial, double gananciaReal) {
        this.id = id;
        this.estudiante = estudiante;
        this.lugar = lugar;
        this.juego = juego;
        this.cantidadAPUNAB = cantidadAPUNAB > 0 ? cantidadAPUNAB : 0;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.otrosApostadores = otrosApostadores != null ? new ArrayList<>(otrosApostadores) : new ArrayList<>();
        this.ganada = ganada;
        this.finalizada = finalizada;
        this.gananciaPotencial = gananciaPotencial;
        this.gananciaReal = gananciaReal;
    }
    
    public boolean crearApuesta(Usuario estudiante, Lugar lugar, Juego juego, double cantidadAPUNAB, List<Usuario> otrosApostadores) {
        if (estudiante == null || lugar == null || juego == null || cantidadAPUNAB <= 0) {
            return false;
        }

        if (estudiante.getSaldoAPUNAB() < cantidadAPUNAB) {
            return false;
        }

        this.estudiante = estudiante;
        this.lugar = lugar;
        this.juego = juego;
        this.cantidadAPUNAB = cantidadAPUNAB;
        this.fecha = LocalDateTime.now();
        
        if (otrosApostadores != null) {
            this.otrosApostadores.clear();
            this.otrosApostadores.addAll(otrosApostadores);
        }
        
        this.gananciaPotencial = calcularGananciaPotencial();
        
        estudiante.actualizarSaldo(-cantidadAPUNAB);
        
        estudiante.agregarApuesta(this);
        
        // Aquí se podría llamar a un método que guarde los datos en la BD
        return true;
    }
    
    public boolean actualizarApuesta(double cantidadAPUNAB, List<Usuario> otrosApostadores) {
        if (finalizada) {
            return false;
        }
        
        boolean actualizado = false;
        
        if (cantidadAPUNAB > this.cantidadAPUNAB) {
            double diferencia = cantidadAPUNAB - this.cantidadAPUNAB;
            
            if (estudiante.getSaldoAPUNAB() >= diferencia) {
                estudiante.actualizarSaldo(-diferencia);
                this.cantidadAPUNAB = cantidadAPUNAB;
                this.gananciaPotencial = calcularGananciaPotencial();
                actualizado = true;
            }
        }
        
        if (otrosApostadores != null) {
            this.otrosApostadores.clear();
            this.otrosApostadores.addAll(otrosApostadores);
            actualizado = true;
        }
        
        // Aquí se podría llamar a un método que actualice los datos en la BD
        return actualizado;
    }
    
    public boolean eliminarApuesta() {
        if (finalizada) {
            return false;
        }
        
        estudiante.actualizarSaldo(cantidadAPUNAB);
        
        // Aquí se podría llamar a un método que elimine los datos de la BD
        return true;
    }
    
    public Apuesta consultarApuesta() {
        return this;
    }
    
    public double calcularGananciaPotencial() {
        if (juego != null) {
            return cantidadAPUNAB * juego.getFactorMultiplicador();
        }
        return cantidadAPUNAB; 
    }
    
    public boolean finalizarApuesta(boolean ganada) {
        if (finalizada) {
            return false;
        }
        
        this.ganada = ganada;
        this.finalizada = true;
        
        if (ganada) {
            this.gananciaReal = this.gananciaPotencial;
            estudiante.actualizarSaldo(gananciaReal);
        } else {
            this.gananciaReal = 0;
        }
        
        // Aquí se podría llamar a un método que actualice los datos en la BD
        return true;
    }
    
    public boolean agregarApostador(Usuario apostador) {
        if (apostador != null && !otrosApostadores.contains(apostador) && apostador != this.estudiante) {
            otrosApostadores.add(apostador);
            return true;
        }
        return false;
    }
    
    public boolean eliminarApostador(Usuario apostador) {
        if (apostador != null && otrosApostadores.contains(apostador)) {
            otrosApostadores.remove(apostador);
            return true;
        }
        return false;
    }
    
    public String getId() {
        return id;
    }

    public Usuario getEstudiante() {
        return estudiante;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public Juego getJuego() {
        return juego;
    }

    public double getCantidadAPUNAB() {
        return cantidadAPUNAB;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public List<Usuario> getOtrosApostadores() {
        return new ArrayList<>(otrosApostadores);
    }

    public boolean isGanada() {
        return ganada;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public double getGananciaPotencial() {
        return gananciaPotencial;
    }

    public double getGananciaReal() {
        return gananciaReal;
    }
    
    public String getEstadoTexto() {
        if (!finalizada) {
            return "En progreso";
        } else if (ganada) {
            return "Ganada";
        } else {
            return "Perdida";
        }
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Apuesta apuesta = (Apuesta) o;
        return id.equals(apuesta.id);
    }
    
    public int hashCode() {
        return Objects.hash(id);
    }
    
    public String toString() {
        return "Apuesta{" +
                "id='" + id + '\'' +
                ", estudiante=" + (estudiante != null ? estudiante.getNombre() + " " + estudiante.getApellido() : "null") +
                ", lugar=" + (lugar != null ? lugar.getNombre() : "null") +
                ", juego=" + (juego != null ? juego.getNombre() : "null") +
                ", cantidadAPUNAB=" + cantidadAPUNAB +
                ", fecha=" + fecha +
                ", estado=" + getEstadoTexto() +
                ", gananciaPotencial=" + gananciaPotencial +
                ", gananciaReal=" + gananciaReal +
                '}';
    }
}//TODO: documentar en el readme
