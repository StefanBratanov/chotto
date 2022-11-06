package chotto.template;

import chotto.objects.BatchContribution;
import chotto.objects.Contribution;
import chotto.objects.G2Point;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TemplateEngine {

  private final Mustache typedDataMustache;

  public TemplateEngine(final MustacheFactory mustacheFactory) {
    this.typedDataMustache = mustacheFactory.compile("templates/typedData.mustache");
  }

  public String createTypedData(final BatchContribution batchContribution) {
    final List<Contribution> contributions = batchContribution.getContributions();
    final List<Map<String, Object>> context = new LinkedList<>();
    for (int i = 0; i < contributions.size(); i++) {
      final Contribution contribution = contributions.get(i);
      final Map<String, Object> contributionContext = new HashMap<>();
      contributionContext.put("numG1Powers", contribution.getNumG1Powers());
      contributionContext.put("numG2Powers", contribution.getNumG2Powers());
      contributionContext.put(
          "potPubkey", contribution.getPotPubkey().map(G2Point::toHexString).orElseThrow());
      if (i != contributions.size() - 1) {
        contributionContext.put("comma", true);
      }
      context.add(contributionContext);
    }
    final StringWriter writer = new StringWriter();
    return typedDataMustache.execute(writer, Map.of("contribution", context)).toString();
  }
}
