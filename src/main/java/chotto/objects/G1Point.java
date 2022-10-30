package chotto.objects;

import java.util.Objects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import supranational.blst.P1;

public class G1Point {

  public static G1Point fromHexString(final String hexString) {
    final Bytes bytes = Bytes.fromHexString(hexString);
    return new G1Point(new P1(bytes.toArrayUnsafe()));
  }

  private final P1 p1;

  private G1Point(final P1 p1) {
    this.p1 = p1;
  }

  public Bytes toBytesCompressed() {
    return Bytes.wrap(p1.compress());
  }

  public String toHexString() {
    return toBytesCompressed().toHexString();
  }

  public G1Point mul(final UInt256 scalar) {
    return new G1Point(p1.mult(scalar.toBigInteger()));
  }

  public boolean isInPrimeSubgroup() {
    return p1.in_group();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final G1Point g1Point = (G1Point) o;
    return Objects.equals(toBytesCompressed(), g1Point.toBytesCompressed());
  }

  @Override
  public int hashCode() {
    return Objects.hash(toBytesCompressed());
  }
}
