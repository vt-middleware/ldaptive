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

import org.ldaptive.Credential;
import org.ldaptive.control.RequestControl;
import org.ldaptive.sasl.SaslConfig;

/**
 * Handles properties for {@link org.ldaptive.BindConnectionInitializer}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class BindConnectionInitializerPropertyInvoker
  extends AbstractPropertyInvoker
{


  /**
   * Creates a new bind connection initializer property invoker for the supplied
   * class.
   *
   * @param  c  class that has setter methods
   */
  public BindConnectionInitializerPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  /** {@inheritDoc} */
  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (SaslConfig.class.isAssignableFrom(type)) {
        if ("null".equals(value)) {
          newValue = null;
        } else {
          if (PropertyValueParser.isParamsOnlyConfig(value)) {
            final PropertyValueParser configParser = new PropertyValueParser(
              value,
              "org.ldaptive.sasl.SaslConfig");
            newValue = configParser.initializeType();
          } else if (PropertyValueParser.isConfig(value)) {
            final PropertyValueParser configParser = new PropertyValueParser(
              value);
            newValue = configParser.initializeType();
          } else {
            newValue = instantiateType(SaslConfig.class, value);
          }
        }
      } else if (RequestControl[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(
          RequestControl.class,
          value);
      } else if (Credential.class.isAssignableFrom(type)) {
        newValue = new Credential(value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
