package chotto.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CeremonyStatus {

  private final int lobbySize;
  private final int numContributions;
  private final String sequencerAddress;

  @JsonCreator
  public CeremonyStatus(
      @JsonProperty("lobby_size") final int lobbySize,
      @JsonProperty("num_contributions") final int numContributions,
      @JsonProperty("sequencer_address") final String sequencerAddress) {
    this.lobbySize = lobbySize;
    this.numContributions = numContributions;
    this.sequencerAddress = sequencerAddress;
  }

  public int getLobbySize() {
    return lobbySize;
  }

  public int getNumContributions() {
    return numContributions;
  }

  public String getSequencerAddress() {
    return sequencerAddress;
  }
}
