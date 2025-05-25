package back_end.Classes;

import back_end.Classes.Lugar;
import back_end.Classes.Usuario;
import back_end.dao.ApuestaDAO;
import back_end.Excepciones.PersistenciaException;
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

    public boolean crearApuesta(Usuario estudiante, Lugar lugar, Juego juego, double cantidadAPUNAB, List<Usuario> otrosApostadores) throws PersistenciaException {
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

        estudiante.actualizarSaldo(-cantidadAPUNAB); // Ahora puede lanzar PersistenciaException

        estudiante.agregarApuesta(this);

        // Integración con la base de datos
        try {
            boolean guardado = ApuestaDAO.guardarApuesta(this);
            if (!guardado) {
                // Si no se pudo guardar, revertir cambios
                estudiante.actualizarSaldo(cantidadAPUNAB);
                return false;
            }
            return true;
        } catch (PersistenciaException e) {
            System.err.println("Error al crear apuesta en BD: " + e.getMessage());
            // Revertir cambios si hay error
            estudiante.actualizarSaldo(cantidadAPUNAB);
            return false;
        }
    }

    public boolean actualizarApuesta(double cantidadAPUNAB, List<Usuario> otrosApostadores) throws PersistenciaException {
        if (finalizada) {
            return false;
        }

        boolean actualizado = false;
        double cantidadAnterior = this.cantidadAPUNAB;

        if (cantidadAPUNAB > this.cantidadAPUNAB) {
            double diferencia = cantidadAPUNAB - this.cantidadAPUNAB;

            if (estudiante.getSaldoAPUNAB() >= diferencia) {
                estudiante.actualizarSaldo(-diferencia); // Puede lanzar PersistenciaException
                this.cantidadAPUNAB = cantidadAPUNAB;
                this.gananciaPotencial = calcularGananciaPotencial();
                actualizado = true;
            }
        } else if (cantidadAPUNAB < this.cantidadAPUNAB && cantidadAPUNAB > 0) {
            // Permitir reducir la apuesta
            double diferencia = this.cantidadAPUNAB - cantidadAPUNAB;
            estudiante.actualizarSaldo(diferencia); // Puede lanzar PersistenciaException
            this.cantidadAPUNAB = cantidadAPUNAB;
            this.gananciaPotencial = calcularGananciaPotencial();
            actualizado = true;
        }

        if (otrosApostadores != null) {
            this.otrosApostadores.clear();
            this.otrosApostadores.addAll(otrosApostadores);
            actualizado = true;
        }

        // Integración con la base de datos
        if (actualizado) {
            try {
                boolean actualizadoBD = ApuestaDAO.actualizarApuesta(this);
                if (!actualizadoBD) {
                    // Si no se pudo actualizar en BD, revertir cambios
                    if (cantidadAPUNAB != cantidadAnterior) {
                        double diferencia = cantidadAnterior - this.cantidadAPUNAB;
                        estudiante.actualizarSaldo(diferencia);
                        this.cantidadAPUNAB = cantidadAnterior;
                        this.gananciaPotencial = calcularGananciaPotencial();
                    }
                    return false;
                }
                return true;
            } catch (PersistenciaException e) {
                System.err.println("Error al actualizar apuesta en BD: " + e.getMessage());
                // Revertir cambios si hay error
                if (cantidadAPUNAB != cantidadAnterior) {
                    double diferencia = cantidadAnterior - this.cantidadAPUNAB;
                    estudiante.actualizarSaldo(diferencia);
                    this.cantidadAPUNAB = cantidadAnterior;
                    this.gananciaPotencial = calcularGananciaPotencial();
                }
                return false;
            }
        }

        return actualizado;
    }

    public boolean eliminarApuesta() throws PersistenciaException {
        if (finalizada) {
            return false;
        }

        estudiante.actualizarSaldo(cantidadAPUNAB); // Puede lanzar PersistenciaException

        // Integración con la base de datos
        try {
            boolean eliminado = ApuestaDAO.eliminarApuesta(this.id);
            if (!eliminado) {
                // Si no se pudo eliminar de BD, revertir cambios
                estudiante.actualizarSaldo(-cantidadAPUNAB);
                return false;
            }
            return true;
        } catch (PersistenciaException e) {
            System.err.println("Error al eliminar apuesta de BD: " + e.getMessage());
            // Revertir cambios si hay error
            estudiante.actualizarSaldo(-cantidadAPUNAB);
            return false;
        }
    }

    public Apuesta consultarApuesta() {
        // Consultar la apuesta más actualizada desde la base de datos
        try {
            Apuesta apuestaActualizada = ApuestaDAO.buscarPorId(this.id);
            if (apuestaActualizada != null) {
                return apuestaActualizada;
            }
        } catch (PersistenciaException e) {
            System.err.println("Error al consultar apuesta desde BD: " + e.getMessage());
        }
        // Si hay error o no se encuentra, retornar la instancia actual
        return this;
    }

    public double calcularGananciaPotencial() {
        if (juego != null) {
            return cantidadAPUNAB * juego.getFactorMultiplicador();
        }
        return cantidadAPUNAB;
    }

    public boolean finalizarApuesta(boolean ganada) throws PersistenciaException {
        if (finalizada) {
            return false;
        }

        this.ganada = ganada;
        this.finalizada = true;

        if (ganada) {
            this.gananciaReal = this.gananciaPotencial;
            estudiante.actualizarSaldo(gananciaReal); // Puede lanzar PersistenciaException
        } else {
            this.gananciaReal = 0;
        }

        // Integración con la base de datos
        try {
            boolean finalizada = ApuestaDAO.finalizarApuesta(this.id, ganada, this.gananciaReal);
            if (!finalizada) {
                // Si no se pudo finalizar en BD, revertir cambios
                this.ganada = false;
                this.finalizada = false;
                if (ganada) {
                    estudiante.actualizarSaldo(-this.gananciaReal);
                }
                this.gananciaReal = 0;
                return false;
            }
            return true;
        } catch (PersistenciaException e) {
            System.err.println("Error al finalizar apuesta en BD: " + e.getMessage());
            // Revertir cambios si hay error
            this.ganada = false;
            this.finalizada = false;
            if (ganada) {
                estudiante.actualizarSaldo(-this.gananciaReal);
            }
            this.gananciaReal = 0;
            return false;
        }
    }

    public boolean agregarApostador(Usuario apostador) {
        if (apostador != null && !otrosApostadores.contains(apostador) && apostador != this.estudiante) {
            otrosApostadores.add(apostador);

            // Actualizar en base de datos
            try {
                return ApuestaDAO.actualizarApuesta(this);
            } catch (PersistenciaException e) {
                System.err.println("Error al agregar apostador en BD: " + e.getMessage());
                // Revertir cambio si hay error
                otrosApostadores.remove(apostador);
                return false;
            }
        }
        return false;
    }

    public boolean eliminarApostador(Usuario apostador) {
        if (apostador != null && otrosApostadores.contains(apostador)) {
            otrosApostadores.remove(apostador);

            // Actualizar en base de datos
            try {
                return ApuestaDAO.actualizarApuesta(this);
            } catch (PersistenciaException e) {
                System.err.println("Error al eliminar apostador en BD: " + e.getMessage());
                // Revertir cambio si hay error
                otrosApostadores.add(apostador);
                return false;
            }
        }
        return false;
    }

    public static Apuesta buscarApuestaPorId(String id) throws PersistenciaException {
        return ApuestaDAO.buscarPorId(id);
    }

    public static List<Apuesta> obtenerApuestasDeUsuario(String usuarioId) throws PersistenciaException {
        return ApuestaDAO.buscarPorUsuario(usuarioId);
    }

    public static List<Apuesta> obtenerTodasLasApuestas() throws PersistenciaException {
        return ApuestaDAO.obtenerTodasLasApuestas();
    }

    public static List<Apuesta> obtenerApuestasActivas() throws PersistenciaException {
        return ApuestaDAO.obtenerApuestasActivas();
    }

    public static boolean eliminarApuestasPorUsuario(String usuarioId) throws PersistenciaException {
        return ApuestaDAO.eliminarApuestasPorUsuario(usuarioId);
    }

    public static boolean existeApuesta(String id) {
        return ApuestaDAO.existeApuesta(id);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Apuesta apuesta = (Apuesta) o;
        return id.equals(apuesta.id);
    }

    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString() {
        return "Apuesta{"
                + "id='" + id + '\''
                + ", estudiante=" + (estudiante != null ? estudiante.getNombre() + " " + estudiante.getApellido() : "null")
                + ", lugar=" + (lugar != null ? lugar.getNombre() : "null")
                + ", juego=" + (juego != null ? juego.getNombre() : "null")
                + ", cantidadAPUNAB=" + cantidadAPUNAB
                + ", fecha=" + fecha
                + ", estado=" + getEstadoTexto()
                + ", gananciaPotencial=" + gananciaPotencial
                + ", gananciaReal=" + gananciaReal
                + '}';
    }
}//TODO: documentar en el readme
