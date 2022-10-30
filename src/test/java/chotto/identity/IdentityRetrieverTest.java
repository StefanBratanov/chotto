package chotto.identity;

import chotto.auth.Provider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityRetrieverTest {

  @Test
  public void createsEthereumInstance() {
    final IdentityRetriever instance = IdentityRetriever.create(Provider.ETHEREUM, null, null);
    assertThat(instance).isInstanceOf(EthIdentityRetriever.class);
  }

  @Test
  public void createsGithubInstance() {
    final IdentityRetriever instance = IdentityRetriever.create(Provider.GITHUB, null, null);
    assertThat(instance).isInstanceOf(GitHubIdentityRetriever.class);
  }
}
