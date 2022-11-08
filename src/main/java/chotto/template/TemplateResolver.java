package chotto.template;

import chotto.objects.BatchContribution;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.ResourceCodeResolver;

public class TemplateResolver {

  private final TemplateEngine templateEngine;

  public TemplateResolver() {
    final CodeResolver codeResolver = new ResourceCodeResolver("templates");
    this.templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
  }

  public String createTypedData(final BatchContribution batchContribution) {
    final TemplateOutput output = new StringOutput();
    templateEngine.render("typedData.jte", batchContribution, output);
    return output.toString();
  }

  public String createSignContributionHtml(
      final String ethAddress, final String typedData, final String callbackPath) {
    final TemplateOutput output = new StringOutput();
    final SignContributionHtmlModel model = new SignContributionHtmlModel();
    model.ethAddress = ethAddress;
    model.typedData = typedData;
    model.callbackPath = callbackPath;
    templateEngine.render("signContribution.jte", model, output);
    return output.toString();
  }

  public static class SignContributionHtmlModel {
    public String ethAddress;
    public String typedData;
    public String callbackPath;
  }
}
