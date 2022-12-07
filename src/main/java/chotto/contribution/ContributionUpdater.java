package chotto.contribution;

import chotto.Constants;
import chotto.objects.Contribution;
import chotto.objects.G1Point;
import chotto.objects.G2Point;
import chotto.objects.PowersOfTau;
import org.apache.tuweni.units.bigints.UInt256;

public class ContributionUpdater {

  public static void updatePowersOfTau(final Contribution contribution, final UInt256 secret) {
    /// Updates the Powers of Tau within a sub-ceremony by multiplying each with a successive power
    // of the secret x.
    UInt256 power = UInt256.ONE;
    for (int i = 0; i < contribution.getNumG1Powers(); i++) {
      final PowersOfTau powersOfTau = contribution.getPowersOfTau();
      // Update G1 Powers
      final G1Point[] g1Powers = powersOfTau.getG1Powers();
      final G1Point currentG1Power = g1Powers[i];
      g1Powers[i] = currentG1Power.mul(power);
      // Update G2 Powers
      if (i < contribution.getNumG2Powers()) {
        final G2Point[] g2Powers = powersOfTau.getG2Powers();
        final G2Point currentG2Power = g2Powers[i];
        g2Powers[i] = currentG2Power.mul(power);
      }
      power = power.multiplyMod(secret, Constants.CURVE_ORDER);
    }
  }
}
