package chotto;

import java.math.BigInteger;
import org.apache.tuweni.units.bigints.UInt256;

public class Constants {

  public static final UInt256 CURVE_ORDER =
      UInt256.valueOf(
          new BigInteger(
              "52435875175126190479447740508185965837690552500527637822603658699938581184513"));

  public static final String IRTF_BLS_CYPHERSUITE = "BLS_SIG_BLS12381G1_XMD:SHA-256_SSWU_RO_POP_";
}
