/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.props;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;
import org.ldaptive.pool.PoolConfig;

/**
 * Reads properties specific to {@link PoolConfig} and returns an initialized
 * object of that type.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public final class PoolConfigPropertySource
  extends AbstractPropertySource<PoolConfig>
{

  /** Invoker for ldap pool config. */
  private static final SimplePropertyInvoker INVOKER =
    new SimplePropertyInvoker(PoolConfig.class);


  /**
   * Creates a new ldap pool config property source using the default properties
   * file.
   *
   * @param  pc  pool config to invoke properties on
   */
  public PoolConfigPropertySource(final PoolConfig pc)
  {
    this(pc, PROPERTIES_FILE);
  }


  /**
   * Creates a new ldap pool config property source.
   *
   * @param  pc  pool config to invoke properties on
   * @param  paths  to read properties from
   */
  public PoolConfigPropertySource(final PoolConfig pc, final String... paths)
  {
    this(pc, loadProperties(paths));
  }


  /**
   * Creates a new ldap pool config property source.
   *
   * @param  pc  pool config to invoke properties on
   * @param  readers  to read properties from
   */
  public PoolConfigPropertySource(final PoolConfig pc, final Reader... readers)
  {
    this(pc, loadProperties(readers));
  }


  /**
   * Creates a new ldap pool config property source.
   *
   * @param  pc  pool config to invoke properties on
   * @param  props  to read properties from
   */
  public PoolConfigPropertySource(final PoolConfig pc, final Properties props)
  {
    this(pc, PropertyDomain.POOL, props);
  }


  /**
   * Creates a new ldap pool config property source.
   *
   * @param  pc  pool config to invoke properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public PoolConfigPropertySource(
    final PoolConfig pc,
    final PropertyDomain domain,
    final Properties props)
  {
    super(pc, domain, props);
  }


  /** {@inheritDoc} */
  @Override
  public void initialize()
  {
    initializeObject(INVOKER);
  }


  /**
   * Returns the property names for this property source.
   *
   * @return  all property names
   */
  public static Set<String> getProperties()
  {
    return INVOKER.getProperties();
  }
}
