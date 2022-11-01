package chotto.objects;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SequencerError {

  private final String code;
  private final String message;

  @JsonCreator
  public SequencerError(
      @JsonProperty(value = "code") final String code,
      @JsonProperty(value = "message") @JsonAlias({"error"}) final String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
