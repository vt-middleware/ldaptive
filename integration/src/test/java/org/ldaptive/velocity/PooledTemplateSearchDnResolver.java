/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.velocity;

import java.util.Arrays;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;

/**
 * {@link Template} based pooled search dn resolver.
 *
 * @author  Middleware Services
 */
public class PooledTemplateSearchDnResolver extends AbstractTemplateSearchDnResolver
  implements PooledConnectionFactoryManager
{

  /** Connection factory. */
  private PooledConnectionFactory factory;


  /**
   * Creates a new pooled template search DN resolver.
   *
   * @param  engine  velocity engine
   * @param  filter  filter template
   *
   * @throws  VelocityException  if velocity is not configured properly or the filter template is invalid
   */
  public PooledTemplateSearchDnResolver(final VelocityEngine engine, final String filter)
    throws VelocityException
  {
    super(engine, filter);
  }


  /**
   * Creates a new pooled template search DN resolver.
   *
   * @param  cf  connection factory
   * @param  engine  velocity engine
   * @param  filter  filter template
   *
   * @throws  VelocityException  if velocity is not configured properly or the filter template is invalid
   */
  public PooledTemplateSearchDnResolver(
    final PooledConnectionFactory cf,
    final VelocityEngine engine,
    final String filter)
    throws VelocityException
  {
    super(engine, filter);
    setConnectionFactory(cf);
  }


  @Override
  public PooledConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  @Override
  public void setConnectionFactory(final PooledConnectionFactory cf)
  {
    factory = cf;
  }


  @Override
  protected Connection getConnection()
    throws LdapException
  {
    return factory.getConnection();
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
