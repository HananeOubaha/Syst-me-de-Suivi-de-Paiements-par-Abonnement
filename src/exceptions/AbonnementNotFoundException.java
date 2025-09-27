package exceptions;

public class AbonnementNotFoundException extends RuntimeException {
    public AbonnementNotFoundException(String message) {
        super(message);
    }
}
