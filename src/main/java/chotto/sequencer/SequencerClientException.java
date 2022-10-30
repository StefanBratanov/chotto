package chotto.sequencer;

public class SequencerClientException extends RuntimeException {

  public SequencerClientException(final String message) {
    super(message);
  }

  public SequencerClientException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
