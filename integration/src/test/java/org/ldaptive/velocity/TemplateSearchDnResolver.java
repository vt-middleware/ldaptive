/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.velocity;

import java.util.Arrays;
import org.apache.velocity.app.VelocityEngine;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.LdapException;

/**
 * {@link Template} based search dn resolver.
 *
 * @author  Middleware Services
 */
public class TemplateSearchDnResolver extends AbstractTemplateSearchDnResolver implements ConnectionFactoryManager
{

  /** Connection factory. */
  private ConnectionFactory factory;


  /**
   * Creates a new template search DN resolver.
   *
   * @param  engine  velocity engine
   * @param  filter  filter template
   *
   * @throws  VelocityException  if velocity is not configured properly or the filter template is invalid
   */
  public TemplateSearchDnResolver(final VelocityEngine engine, final String filter)
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
  {
    super(engine, filter);
    setConnectionFactory(cf);
  }


  @Override
  public ConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  @Override
  public void setConnectionFactory(final ConnectionFactory cf)
  {
    factory = cf;
  }


  @Override
  protected Connection getConnection()
    throws LdapException
  {
    final Connection conn = factory.getConnection();
    conn.open();
    return conn;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::factory=%s, templateName=%s, baseDn=%s, userFilter=%s, userFilterParameters=%s, " +
          "allowMultipleDns=%s, subtreeSearch=%s, derefAliases=%s, referralHandler=%s]",
        getClass().getName(),
        hashCode(),
        factory,
        getTemplate().getTemplateName(),
        getBaseDn(),
        getUserFilter(),
        Arrays.toString(getUserFilterParameters()),
        getAllowMultipleDns(),
        getSubtreeSearch(),
        getDerefAliases(),
        getReferralHandler());
  }
}
