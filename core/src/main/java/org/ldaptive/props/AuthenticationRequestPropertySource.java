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
import org.ldaptive.auth.AuthenticationRequest;

/**
 * Reads properties specific to {@link org.ldaptive.auth.AuthenticationRequest}
 * and returns an initialized object of that type.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public final class AuthenticationRequestPropertySource
  extends AbstractPropertySource<AuthenticationRequest>
{

  /** Invoker for authentication request. */
  private static final AuthenticationRequestPropertyInvoker INVOKER =
    new AuthenticationRequestPropertyInvoker(AuthenticationRequest.class);


  /**
   * Creates a new authentication request property source using the default
   * properties file.
   *
   * @param  request  authentication request to set properties on
   */
  public AuthenticationRequestPropertySource(
    final AuthenticationRequest request)
  {
    this(request, PROPERTIES_FILE);
  }


  /**
   * Creates a new authentication request property source.
   *
   * @param  request  authentication request to set properties on
   * @param  paths  to read properties from
   */
  public AuthenticationRequestPropertySource(
    final AuthenticationRequest request,
    final String... paths)
  {
    this(request, loadProperties(paths));
  }


  /**
   * Creates a new authentication request property source.
   *
   * @param  request  authentication request to set properties on
   * @param  readers  to read properties from
   */
  public AuthenticationRequestPropertySource(
    final AuthenticationRequest request,
    final Reader... readers)
  {
    this(request, loadProperties(readers));
  }


  /**
   * Creates a new authentication request property source.
   *
   * @param  request  authentication request to set properties on
   * @param  props  to read properties from
   */
  public AuthenticationRequestPropertySource(
    final AuthenticationRequest request,
    final Properties props)
  {
    this(request, PropertyDomain.AUTH, props);
  }


  /**
   * Creates a new authentication request property source.
   *
   * @param  request  authentication request to set properties on
   * @param  domain  that properties are in
   * @param  props  to read properties from
   */
  public AuthenticationRequestPropertySource(
    final AuthenticationRequest request,
    final PropertyDomain domain,
    final Properties props)
  {
    super(request, domain, props);
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
