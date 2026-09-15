package reginaldo.orbit.api.exception;

public class ProjectHasTasksException extends ConflictException {
    public ProjectHasTasksException(String message) {
        super(message);
    }
}
