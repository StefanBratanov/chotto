package chotto.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class GitHubIdentityRetriever implements IdentityRetriever {

  private static final String ID_FIELD = "id";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public GitHubIdentityRetriever(final HttpClient httpClient, final ObjectMapper objectMapper) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
  }

  @Override
  public String getIdentity(final String githubUsername) {
    try {
      final HttpRequest request =
          HttpRequest.newBuilder()
              .uri(new URI("https://api.github.com/users/" + githubUsername))
              .GET()
              .build();
      final HttpResponse<InputStream> httpResponse =
          httpClient.send(request, BodyHandlers.ofInputStream());
      if (httpResponse.statusCode() == 404) {
        throw new IllegalArgumentException(githubUsername + " is not a valid GitHub username");
      }
      final JsonNode response = objectMapper.readTree(httpResponse.body());
      if (!response.has(ID_FIELD)) {
        throw new IllegalArgumentException(
            response + " was not an expected response from Github. 'id' field is missing.");
      }
      final long id = response.get(ID_FIELD).asLong();
      return String.format("git|%s|%s", id, "@" + githubUsername);
    } catch (final IOException | InterruptedException | URISyntaxException ex) {
      throw new IllegalStateException(
          "Error when retrieving Github identity for " + githubUsername, ex);
    }
  }
}
