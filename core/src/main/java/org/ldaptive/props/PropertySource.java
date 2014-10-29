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

/**
 * Interface for property driven object initialization.
 *
 * @param  <T>  type of object to invoke properties on
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface PropertySource<T>
{

  /** Enum to define the domain for properties. */
  public enum PropertyDomain {

    /** ldap property domain. */
    LDAP("org.ldaptive."),

    /** auth property domain. */
    AUTH("org.ldaptive.auth."),

    /** pool property domain. */
    POOL("org.ldaptive.pool.");

    /** properties domain. */
    private final String domain;


    /**
     * Creates a new property domain.
     *
     * @param  s  properties domain
     */
    PropertyDomain(final String s)
    {
      domain = s;
    }


    /**
     * Returns the properties domain value.
     *
     * @return  properties domain
     */
    public String value()
    {
      return domain;
    }
  }


  /** Initializes the object for this property source. */
  void initialize();
}
