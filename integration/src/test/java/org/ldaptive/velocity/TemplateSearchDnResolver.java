/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.velocity;

import java.util.Arrays;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionFactoryManager;

/**
 * {@link Template} based search dn resolver.
 *
 * @author  Middleware Services
 */
public class TemplateSearchDnResolver extends AbstractTemplateSearchDnResolver implements ConnectionFactoryManager
{


  /**
   * Creates a new template search DN resolver.
   *
   * @param  engine  velocity engine
   * @param  filter  filter template
   *
   * @throws  VelocityException  if velocity is not configured properly or the filter template is invalid
   */
  public TemplateSearchDnResolver(final VelocityEngine engine, final String filter)
    throws VelocityException
  {
    super(engine, filter);
  }


  /**
   * Creates a new template search DN resolver.
   *
   * @param  cf  connection factory
   * @param  engine  velocity engine
   * @param  filter  filter template
   *
   * @throws  VelocityException  if velocity is not configured properly or the filter template is invalid
   */
  public TemplateSearchDnResolver(final ConnectionFactory cf, final VelocityEngine engine, final String filter)
    throws VelocityException
  {
    super(engine, filter);
    setConnectionFactory(cf);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("factory=").append(getConnectionFactory()).append(", ")
      .append("templateName=").append(getTemplate().getTemplateName()).append(", ")
      .append("baseDn=").append(getBaseDn()).append(", ")
      .append("userFilter=").append(getUserFilter()).append(", ")
      .append("userFilterParameters=").append(Arrays.toString(getUserFilterParameters())).append(", ")
      .append("allowMultipleDns=").append(getAllowMultipleDns()).append(", ")
      .append("subtreeSearch=").append(getSubtreeSearch()).append(", ")
      .append("derefAliases=").append(getDerefAliases()).append(", ")
      .append("searchResultHandlers=").append(Arrays.toString(getSearchResultHandlers())).append("]").toString();
  }
}
