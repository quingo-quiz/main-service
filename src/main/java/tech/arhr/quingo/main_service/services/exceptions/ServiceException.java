package tech.arhr.quingo.main_service.services.exceptions;

public class ServiceException extends RuntimeException {
  public ServiceException(String message) {
    super(message);
  }
}
