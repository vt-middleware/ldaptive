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
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.control.RequestControl;
import org.ldaptive.sasl.SaslConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes a connection by performing a bind operation. Useful if you need
 * all connections to bind as the same principal.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class BindConnectionInitializer implements ConnectionInitializer
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** DN to bind as before performing operations. */
  private String bindDn;

  /** Credential for the bind DN. */
  private Credential bindCredential;

  /** Configuration for bind SASL authentication. */
  private SaslConfig bindSaslConfig;

  /** Bind controls. */
  private RequestControl[] bindControls;


  /** Default constructor. */
  public BindConnectionInitializer() {}


  /**
   * Creates a new bind connection initializer.
   *
   * @param  dn  bind dn
   * @param  credential  bind credential
   */
  public BindConnectionInitializer(final String dn, final Credential credential)
  {
    setBindDn(dn);
    setBindCredential(credential);
  }


  /**
   * Returns the bind DN.
   *
   * @return  DN to bind as
   */
  public String getBindDn()
  {
    return bindDn;
  }


  /**
   * Sets the bind DN to authenticate as before performing operations.
   *
   * @param  dn  to bind as
   */
  public void setBindDn(final String dn)
  {
    logger.trace("setting bindDn: {}", dn);
    bindDn = dn;
  }


  /**
   * Returns the credential used with the bind DN.
   *
   * @return  bind DN credential
   */
  public Credential getBindCredential()
  {
    return bindCredential;
  }


  /**
   * Sets the credential of the bind DN.
   *
   * @param  credential  to use with bind DN
   */
  public void setBindCredential(final Credential credential)
  {
    logger.trace("setting bindCredential: <suppressed>");
    bindCredential = credential;
  }


  /**
   * Returns the bind sasl config.
   *
   * @return  sasl config
   */
  public SaslConfig getBindSaslConfig()
  {
    return bindSaslConfig;
  }


  /**
   * Sets the bind sasl config.
   *
   * @param  config  sasl config
   */
  public void setBindSaslConfig(final SaslConfig config)
  {
    logger.trace("setting bindSaslConfig: {}", config);
    bindSaslConfig = config;
  }


  /**
   * Returns the bind controls.
   *
   * @return  controls
   */
  public RequestControl[] getBindControls()
  {
    return bindControls;
  }


  /**
   * Sets the bind controls.
   *
   * @param  c  controls to set
   */
  public void setBindControls(final RequestControl... c)
  {
    logger.trace("setting bindControls: {}", Arrays.toString(c));
    bindControls = c;
  }


  /** {@inheritDoc} */
  @Override
  public Response<Void> initialize(final Connection c)
    throws LdapException
  {
    final BindRequest request = new BindRequest();
    request.setDn(bindDn);
    request.setCredential(bindCredential);
    request.setSaslConfig(bindSaslConfig);
    request.setControls(bindControls);

    final BindOperation op = new BindOperation(c);
    op.setOperationExceptionHandler(null);
    return op.execute(request);
  }


  /**
   * Returns whether this connection initializer contains any configuration
   * data.
   *
   * @return  whether all properties are null
   */
  public boolean isEmpty()
  {
    return
      bindDn == null && bindCredential == null && bindSaslConfig == null &&
      bindControls == null;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::bindDn=%s, bindSaslConfig=%s, bindControls=%s]",
        getClass().getName(),
        hashCode(),
        bindDn,
        bindSaslConfig,
        Arrays.toString(bindControls));
  }
}
