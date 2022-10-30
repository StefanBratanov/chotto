package chotto.identity;

public class EthIdentityRetriever implements IdentityRetriever {

  @Override
  public String getIdentity(final String ethAddress) {
    if (!ethAddress.startsWith("0x")) {
      throw new IllegalArgumentException("Ethereum address must start with '0x'");
    }
    if (ethAddress.length() != 42) {
      throw new IllegalArgumentException("Ethereum address must be a 42-character hexadecimal");
    }

    return "eth|" + ethAddress.toLowerCase();
  }
}
