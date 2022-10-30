package chotto.serialization;

import chotto.objects.G1Point;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class G1PointDeserializer extends JsonDeserializer<G1Point> {

  @Override
  public G1Point deserialize(final JsonParser p, final DeserializationContext ctxt)
      throws IOException {
    return G1Point.fromHexString(p.getValueAsString());
  }
}
