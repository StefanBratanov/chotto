package chotto.identity;

import java.net.http.HttpClient;
import java.util.UUID;

import chotto.serialization.ChottoObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubIdentityRetrieverTest {

  private final HttpClient httpClient = HttpClient.newBuilder().build();

  private final GitHubIdentityRetriever gitHubIdentityRetriever =
      new GitHubIdentityRetriever(httpClient, ChottoObjectMapper.getInstance());

  @Test
  public void getsIdentity() {
    final String identity = gitHubIdentityRetriever.getIdentity("StefanBratanov");
    assertThat(identity).isEqualTo("git|14827647|@StefanBratanov");
  }

  @Test
  public void failsIfNotValidGitHubUsername() {
    final IllegalArgumentException exception =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> gitHubIdentityRetriever.getIdentity(UUID.randomUUID().toString()));

    assertThat(exception).hasMessageEndingWith("is not a valid GitHub username");
  }
}
