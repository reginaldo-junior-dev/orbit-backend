package reginaldo.orbit.api.exception;

public class CannotModifySelfException extends BadRequestException {
    public CannotModifySelfException(String message) {
        super(message);
    }
}
