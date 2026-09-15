package reginaldo.orbit.api.exception;

public class EmailAlreadyExists extends ConflictException {
    public EmailAlreadyExists(String message) {
        super(message);
    }
}
