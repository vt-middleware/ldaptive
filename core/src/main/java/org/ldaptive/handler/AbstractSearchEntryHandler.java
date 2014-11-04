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
package org.ldaptive.handler;

import java.util.HashSet;
import java.util.Set;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for entry handlers which simply returns values unaltered.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public abstract class AbstractSearchEntryHandler implements SearchEntryHandler
{

  /** Log for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());


  /** {@inheritDoc} */
  @Override
  public HandlerResult<SearchEntry> handle(
    final Connection conn,
    final SearchRequest request,
    final SearchEntry entry)
    throws LdapException
  {
    if (entry != null) {
      entry.setDn(handleDn(conn, request, entry));
      handleAttributes(conn, request, entry);
    }
    return new HandlerResult<>(entry);
  }


  /**
   * Handle the dn of a search entry.
   *
   * @param  conn  the search was performed on
   * @param  request  used to find the search entry
   * @param  entry  search entry to extract the dn from
   *
   * @return  handled dn
   */
  protected String handleDn(
    final Connection conn,
    final SearchRequest request,
    final SearchEntry entry)
  {
    return entry.getDn();
  }


  /**
   * Handle the attributes of a search entry.
   *
   * @param  conn  the search was performed on
   * @param  request  used to find the search entry
   * @param  entry  search entry to extract the attributes from
   *
   * @throws  LdapException  if the LDAP returns an error
   */
  protected void handleAttributes(
    final Connection conn,
    final SearchRequest request,
    final SearchEntry entry)
    throws LdapException
  {
    for (LdapAttribute la : entry.getAttributes()) {
      handleAttribute(conn, request, la);
    }
  }


  /**
   * Handle a single attribute.
   *
   * @param  conn  the search was performed on
   * @param  request  used to find the search entry
   * @param  attr  to handle
   *
   * @throws  LdapException  if the LDAP returns an error
   */
  protected void handleAttribute(
    final Connection conn,
    final SearchRequest request,
    final LdapAttribute attr)
    throws LdapException
  {
    if (attr != null) {
      attr.setName(handleAttributeName(conn, request, attr.getName()));
      if (attr.isBinary()) {
        final Set<byte[]> newValues = new HashSet<>(attr.size());
        for (byte[] b : attr.getBinaryValues()) {
          newValues.add(handleAttributeValue(conn, request, b));
        }
        attr.clear();
        attr.addBinaryValues(newValues);
      } else {
        final Set<String> newValues = new HashSet<>(attr.size());
        for (String s : attr.getStringValues()) {
          newValues.add(handleAttributeValue(conn, request, s));
        }
        attr.clear();
        attr.addStringValues(newValues);
      }
    }
  }


  /**
   * Returns the supplied attribute name unaltered.
   *
   * @param  conn  the search was performed on
   * @param  request  used to find the search entry
   * @param  name  to handle
   *
   * @return  handled name
   */
  protected String handleAttributeName(
    final Connection conn,
    final SearchRequest request,
    final String name)
  {
    return name;
  }


  /**
   * Returns the supplied attribute value unaltered.
   *
   * @param  conn  the search was performed on
   * @param  request  used to find the search entry
   * @param  value  to handle
   *
   * @return  handled value
   */
  protected String handleAttributeValue(
    final Connection conn,
    final SearchRequest request,
    final String value)
  {
    return value;
  }


  /**
   * Returns the supplied attribute value unaltered.
   *
   * @param  conn  the search was performed on
   * @param  request  used to find the search entry
   * @param  value  to handle
   *
   * @return  handled value
   */
  protected byte[] handleAttributeValue(
    final Connection conn,
    final SearchRequest request,
    final byte[] value)
  {
    return value;
  }


  /** {@inheritDoc} */
  @Override
  public void initializeRequest(final SearchRequest request) {}


  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object o)
  {
    return LdapUtils.areEqual(this, o);
  }


  /** {@inheritDoc} */
  @Override
  public abstract int hashCode();
}
