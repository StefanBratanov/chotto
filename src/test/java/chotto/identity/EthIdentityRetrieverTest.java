package chotto.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EthIdentityRetrieverTest {

  private final EthIdentityRetriever ethIdentityRetriever = new EthIdentityRetriever();

  @Test
  public void failsIfEthAddressDoesNotStartWith0x() {
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> ethIdentityRetriever.getIdentity("cd3b766ccdd6ae721141f452c550ca635964ce71"));
    assertThat(exception).hasMessage("Ethereum address must start with '0x'");
  }

  @Test
  public void failsIfEthAddressIsNotOfCorrectLength() {
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> ethIdentityRetriever.getIdentity("0x1ABC7154748D1CE5144478CDEB574AE244B939B57"));
    assertThat(exception).hasMessage("Ethereum address must be a 42-character hexadecimal");
  }

  @Test
  public void getsIdentity() {
    final String ethAddress = "0x1ABC7154748D1CE5144478CDEB574AE244B939B5";
    final String identity = ethIdentityRetriever.getIdentity(ethAddress);

    assertThat(identity).isEqualTo("eth|0x1abc7154748d1ce5144478cdeb574ae244b939b5");
  }
}
