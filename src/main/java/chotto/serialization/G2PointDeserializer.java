package chotto.serialization;

import chotto.objects.G2Point;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class G2PointDeserializer extends JsonDeserializer<G2Point> {

  @Override
  public G2Point deserialize(final JsonParser p, final DeserializationContext ctxt)
      throws IOException {
    return G2Point.fromHexString(p.getValueAsString());
  }
}
