package chotto.serialization;

import chotto.objects.BlsSignature;
import chotto.objects.G1Point;
import chotto.objects.G2Point;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class ChottoObjectMapper {

  private ChottoObjectMapper() {}

  private static ObjectMapper INSTANCE;

  public static ObjectMapper getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ObjectMapper();
      final SimpleModule module = new SimpleModule("Chotto");
      module.addSerializer(G1Point.class, new G1PointSerializer());
      module.addDeserializer(G1Point.class, new G1PointDeserializer());
      module.addSerializer(G2Point.class, new G2PointSerializer());
      module.addDeserializer(G2Point.class, new G2PointDeserializer());
      module.addSerializer(BlsSignature.class, new BlsSignatureSerializer());
      module.addDeserializer(BlsSignature.class, new BlsSignatureDeserializer());
      INSTANCE.registerModule(module);
      INSTANCE.registerModule(new Jdk8Module());
      INSTANCE.setSerializationInclusion(Include.NON_NULL);
    }
    return INSTANCE;
  }
}
