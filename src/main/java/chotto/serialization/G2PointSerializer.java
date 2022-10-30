package chotto.serialization;

import chotto.objects.G2Point;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class G2PointSerializer extends JsonSerializer<G2Point> {

  @Override
  public void serialize(
      final G2Point value, final JsonGenerator gen, final SerializerProvider serializers)
      throws IOException {
    gen.writeString(value.toHexString());
  }
}
