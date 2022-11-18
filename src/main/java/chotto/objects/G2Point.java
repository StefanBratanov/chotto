package chotto.objects;

import java.util.Objects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import supranational.blst.P2;

public class G2Point {

  public static G2Point fromHexString(final String hexString) {
    final Bytes bytes = Bytes.fromHexString(hexString);
    return new G2Point(new P2(bytes.toArrayUnsafe()));
  }

  public static G2Point generator() {
    return new G2Point(P2.generator());
  }

  private final P2 p2;

  private G2Point(final P2 p2) {
    this.p2 = p2;
  }

  public Bytes toBytesCompressed() {
    return Bytes.wrap(p2.compress());
  }

  public String toHexString() {
    return toBytesCompressed().toHexString();
  }

  public G2Point mul(final UInt256 scalar) {
    return new G2Point(p2.mult(scalar.toBigInteger()));
  }

  public boolean isInPrimeSubgroup() {
    return p2.in_group();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final G2Point g2Point = (G2Point) o;
    return Objects.equals(toBytesCompressed(), g2Point.toBytesCompressed());
  }

  @Override
  public int hashCode() {
    return Objects.hash(toBytesCompressed());
  }
}
