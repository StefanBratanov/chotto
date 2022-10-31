package chotto.lifecycle;

public class ApiLifecycleException extends RuntimeException {

  public ApiLifecycleException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
