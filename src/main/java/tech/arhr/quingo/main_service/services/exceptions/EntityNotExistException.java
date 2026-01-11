package tech.arhr.quingo.main_service.services.exceptions;

public class EntityNotExistException extends ServiceException {
    public EntityNotExistException() {
        super("Entity not exist");
    }
}
