package tech.arhr.quingo.main_service.services.exceptions;

public class EntityNotExistException extends RuntimeException {
  public EntityNotExistException(String message) {
    super(message);
  }
}
