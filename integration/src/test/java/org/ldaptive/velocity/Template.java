/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.velocity;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

/**
 * Class that encapsulates the details of merging a velocity template with a context.
 *
 * @author  Middleware Services
 */
public class Template
{

  /** VelocityEngine. */
  private final VelocityEngine velocityEngine;

  /** Template name registered in the velocity repository. */
  private final String templateName;


  /**
   * Creates a new template search DN resolver.
   *
   * @param  engine  velocity engine
   * @param  template  template text
   *
   * @throws  VelocityException  if velocity is not configured properly or the filter template is invalid
   */
  public Template(final VelocityEngine engine, final String template)
  {
    // generate a template name and register it
    final StringResourceRepository repository = StringResourceLoader.getRepository();
    if (repository == null) {
      throw new VelocityException(
        "Velocity engine is not configured to load templates from the default StringResourceRepository");
    }
    templateName = generateTemplateName(repository);
    repository.putStringResource(templateName, template, StandardCharsets.UTF_8.name());

    // confirm velocity is configured
    if (!engine.resourceExists(templateName)) {
      throw new VelocityException(
        "Velocity engine is not configured to load templates from the default StringResourceRepository");
    } else {
      try {
        engine.getTemplate(templateName);
      } catch (VelocityException e) {
        throw new VelocityException("Invalid template: " + template, e);
      }
    }
    velocityEngine = engine;
  }


  /**
   * Returns the template name.
   *
   * @return  template name
   */
  public String getTemplateName()
  {
    return templateName;
  }


  /**
   * Returns a template name to use in the supplied repository. The name is selected by generating a random string and
   * confirming that name is not in use.
   *
   * @param  repository  to store the template name
   *
   * @return  template name
   */
  protected String generateTemplateName(final StringResourceRepository repository)
  {
    String name;
    do {
      name = UUID.randomUUID().toString();
    } while(repository.getStringResource(name) != null);
    return name;
  }


  /**
   * Merges {@link #templateName} with the supplied context. See {@link
   * VelocityEngine#mergeTemplate(String, String, Context, Writer)}.
   *
   * @param  context  to merge
   *
   * @return  merged filter template
   */
  public String merge(final VelocityContext context)
  {
    final StringWriter writer = new StringWriter();
    velocityEngine.mergeTemplate(templateName, StandardCharsets.UTF_8.name(), context, writer);
    return writer.toString();
  }
}
