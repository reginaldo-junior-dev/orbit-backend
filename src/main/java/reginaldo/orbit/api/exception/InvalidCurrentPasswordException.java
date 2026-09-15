package reginaldo.orbit.api.exception;

public class InvalidCurrentPasswordException extends BadRequestException {
    public InvalidCurrentPasswordException(String message) {
        super(message);
    }
}
