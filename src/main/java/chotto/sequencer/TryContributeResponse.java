package chotto.sequencer;

import chotto.objects.BatchContribution;
import chotto.objects.SequencerError;
import java.util.Optional;

public class TryContributeResponse {

  private final Optional<BatchContribution> batchContribution;
  private final Optional<SequencerError> sequencerError;

  public TryContributeResponse(
      final Optional<BatchContribution> batchContribution,
      final Optional<SequencerError> sequencerError) {
    this.batchContribution = batchContribution;
    this.sequencerError = sequencerError;
  }

  public Optional<BatchContribution> getBatchContribution() {
    return batchContribution;
  }

  public Optional<SequencerError> getSequencerError() {
    return sequencerError;
  }
}
