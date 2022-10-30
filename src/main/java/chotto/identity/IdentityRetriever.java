package chotto.identity;

import chotto.auth.Provider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;

public interface IdentityRetriever {

  String getIdentity(final String nickname);

  static IdentityRetriever create(
      final Provider provider, final HttpClient httpClient, final ObjectMapper objectMapper) {
    if (provider.equals(Provider.ETHEREUM)) {
      return new EthIdentityRetriever();
    } else if (provider.equals(Provider.GITHUB)) {
      return new GitHubIdentityRetriever(httpClient, objectMapper);
    } else {
      throw new IllegalArgumentException("Can't create identity retriever for " + provider);
    }
  }
}
