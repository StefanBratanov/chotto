package chotto.contribution;

import java.io.InputStream;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;

public class SchemaLoader {

  public static JsonSchema loadSchema() {
    final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
    final InputStream schemaIs =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("contributionSchema.json");
    return factory.getSchema(schemaIs);
  }
}
