package chotto.objects;

import java.util.Objects;
import org.apache.tuweni.bytes.Bytes;
import supranational.blst.P1_Affine;

public class BlsSignature {

  private final P1_Affine ecPoint;

  public static BlsSignature fromHexString(final String hexString) {
    final Bytes bytes = Bytes.fromHexString(hexString);
    return new BlsSignature(new P1_Affine(bytes.toArrayUnsafe()));
  }

  public BlsSignature(final P1_Affine ecPoint) {
    this.ecPoint = ecPoint;
  }

  public P1_Affine getEcPoint() {
    return ecPoint;
  }

  public Bytes toBytesCompressed() {
    return Bytes.wrap(ecPoint.compress());
  }

  public String toHexString() {
    return toBytesCompressed().toHexString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final BlsSignature that = (BlsSignature) o;
    return Objects.equals(toBytesCompressed(), that.toBytesCompressed());
  }

  @Override
  public int hashCode() {
    return Objects.hash(toBytesCompressed());
  }
}
