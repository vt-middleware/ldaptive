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

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.TrustManager;
import org.ldaptive.ssl.CredentialConfig;
import org.ldaptive.ssl.SslConfig;

/**
 * Handles properties for {@link SslConfig}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class SslConfigPropertyInvoker extends AbstractPropertyInvoker
{


  /**
   * Creates a new ssl config property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public SslConfigPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  /** {@inheritDoc} */
  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (CredentialConfig.class.isAssignableFrom(type)) {
        if ("null".equals(value)) {
          newValue = null;
        } else {
          if (CredentialConfigParser.isCredentialConfig(value)) {
            final CredentialConfigParser configParser =
              new CredentialConfigParser(value);
            newValue = configParser.initializeType();
          } else if (PropertyValueParser.isConfig(value)) {
            final PropertyValueParser configParser = new PropertyValueParser(
              value);
            newValue = configParser.initializeType();
          } else {
            newValue = instantiateType(SslConfig.class, value);
          }
        }
      } else if (TrustManager[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(TrustManager.class, value);
      } else if (HandshakeCompletedListener[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(
          HandshakeCompletedListener.class,
          value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
