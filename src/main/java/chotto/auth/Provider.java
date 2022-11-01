package chotto.auth;

import java.util.Arrays;

public enum Provider {
  ETHEREUM("Ethereum"),
  GITHUB("Github");

  private final String providerName;

  Provider(final String providerName) {
    this.providerName = providerName;
  }

  @Override
  public String toString() {
    return providerName;
  }

  public static Provider fromProviderName(final String providerName) {
    return Arrays.stream(Provider.values())
        .filter(provider -> provider.providerName.equalsIgnoreCase(providerName))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown provider: " + providerName));
  }
}
