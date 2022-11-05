package chotto.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class BatchTranscript {

  private final List<Transcript> transcripts;
  private final List<String> participantIds;
  private final List<String> participantEcdsaSignatures;

  @JsonCreator
  public BatchTranscript(
      @JsonProperty("transcripts") final List<Transcript> transcripts,
      @JsonProperty("participantIds") final List<String> participantIds,
      @JsonProperty("participantEcdsaSignatures") final List<String> participantEcdsaSignatures) {
    this.transcripts = transcripts;
    this.participantIds = participantIds;
    this.participantEcdsaSignatures = participantEcdsaSignatures;
  }

  public List<Transcript> getTranscripts() {
    return transcripts;
  }

  public List<String> getParticipantIds() {
    return participantIds;
  }

  public List<String> getParticipantEcdsaSignatures() {
    return participantEcdsaSignatures;
  }
}
