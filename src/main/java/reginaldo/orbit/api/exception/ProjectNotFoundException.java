package reginaldo.orbit.api.exception;

public class ProjectNotFoundException extends ResourceNotFoundException {
    public ProjectNotFoundException(String message) {
        super(message);
    }
}
