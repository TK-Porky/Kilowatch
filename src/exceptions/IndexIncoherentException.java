package exceptions;

/**
 * Exception levée lorsque le nouvel index saisi est inférieur à l'ancien index.
 */
public class IndexIncoherentException extends Exception {
    
    public IndexIncoherentException() {
        super("Erreur : Le nouvel index saisi ne peut pas être inférieur à l'ancien index.");
    }

    public IndexIncoherentException(String message) {
        super(message);
    }
}