/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.velocity;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.ldaptive.SearchFilter;
import org.ldaptive.auth.AbstractSearchDnResolver;
import org.ldaptive.auth.User;

/**
 * Base class for {@link Template} based search dn resolvers.
 *
 * @author  Middleware Services
 */
public abstract class AbstractTemplateSearchDnResolver extends AbstractSearchDnResolver
{

  /** Template. */
  private final Template template;

  /** Event handler used for escaping. */
  private ReferenceInsertionEventHandler eventHandler = new EscapingReferenceInsertionEventHandler();


  /**
   * Creates a new abstract template search DN resolver.
   *
   * @param  engine  velocity engine
   * @param  filter  filter template
   *
   * @throws  VelocityException  if velocity is not configured properly or the filter template is invalid
   */
  public AbstractTemplateSearchDnResolver(final VelocityEngine engine, final String filter)
  {
    template = new Template(engine, filter);
    setUserFilter(filter);
  }


  /**
   * Returns the template.
   *
   * @return  template
   */
  public Template getTemplate()
  {
    return template;
  }


  @Override
  protected SearchFilter createSearchFilter(final User user)
  {
    final SearchFilter filter = new SearchFilter();
    if (user != null && user.getContext() != null) {
      final VelocityContext context = (VelocityContext) user.getContext();
      final EventCartridge cartridge = new EventCartridge();
      cartridge.addEventHandler(eventHandler);
      cartridge.attachToContext(context);
      final String result = template.merge(context);
      if (result != null) {
        filter.setFilter(result.trim());
      }
    } else {
      logger.warn("Search filter cannot be created, user input was empty or null");
    }
    return filter;
  }
}
