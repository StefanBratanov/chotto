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
}
