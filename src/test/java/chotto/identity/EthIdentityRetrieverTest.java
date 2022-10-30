package chotto.identity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EthIdentityRetrieverTest {

  private final EthIdentityRetriever ethIdentityRetriever = new EthIdentityRetriever();

  @Test
  public void getsIdentity() {
    final String ethAddress = "0x1ABC7154748D1CE5144478CDEB574AE244B939B5";
    final String identity = ethIdentityRetriever.getIdentity(ethAddress);

    assertThat(identity).isEqualTo("eth|0x1abc7154748d1ce5144478cdeb574ae244b939b5");
  }
}
