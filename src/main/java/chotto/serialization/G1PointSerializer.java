package chotto.serialization;

import chotto.objects.G1Point;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class G1PointSerializer extends JsonSerializer<G1Point> {

  @Override
  public void serialize(
      final G1Point value, final JsonGenerator gen, final SerializerProvider serializers)
      throws IOException {
    gen.writeString(value.toHexString());
  }
}
