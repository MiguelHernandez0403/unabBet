package back_end.Excepciones;


public class PersistenciaException extends Exception{

    public PersistenciaException() {
        super("Error de persistencia de datos");
    }

    public PersistenciaException(String message) {
        super(message);
    }

    public PersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }
    
}//TODO: documentar en el readme
