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
import java.util.Collection;

/**
 * Contains the data required to perform an ldap add operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class AddRequest extends AbstractRequest
{

  /** DN to create. */
  private String createDn;

  /** Attributes to add to the newly created entry. */
  private Collection<LdapAttribute> attributes;


  /** Default constructor. */
  public AddRequest() {}


  /**
   * Creates a new add request.
   *
   * @param  dn  to create
   * @param  attrs  attributes to add
   */
  public AddRequest(final String dn, final Collection<LdapAttribute> attrs)
  {
    setDn(dn);
    setLdapAttributes(attrs);
  }


  /**
   * Returns the DN to create.
   *
   * @return  DN
   */
  public String getDn()
  {
    return createDn;
  }


  /**
   * Sets the DN to create.
   *
   * @param  dn  to create
   */
  public void setDn(final String dn)
  {
    createDn = dn;
  }


  /**
   * Returns the attributes to add.
   *
   * @return  attributes
   */
  public Collection<LdapAttribute> getLdapAttributes()
  {
    return attributes;
  }


  /**
   * Sets the attributes to add.
   *
   * @param  attrs  to add
   */
  public void setLdapAttributes(final Collection<LdapAttribute> attrs)
  {
    attributes = attrs;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::createDn=%s, attributes=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        createDn,
        attributes,
        Arrays.toString(getControls()));
  }
}
