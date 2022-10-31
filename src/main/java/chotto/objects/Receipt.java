package chotto.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Receipt {

  private final String receipt;
  private final String signature;

  @JsonCreator
  public Receipt(
      @JsonProperty("receipt") final String receipt,
      @JsonProperty("signature") final String signature) {
    this.receipt = receipt;
    this.signature = signature;
  }

  public String getReceipt() {
    return receipt;
  }

  public String getSignature() {
    return signature;
  }
}
