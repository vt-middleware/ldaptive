/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchRequest;

/**
 * Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020.
 *
 * @author  Middleware Services
 */
public class DnAttributeEntryHandler extends AbstractSearchEntryHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 823;

  /** Attribute name for the entry dn. */
  private String dnAttributeName = "entryDN";

  /**
   * Whether to add the entry dn if an attribute of the same name exists.
   */
  private boolean addIfExists;


  /**
   * Returns the DN attribute name.
   *
   * @return  DN attribute name
   */
  public String getDnAttributeName()
  {
    return dnAttributeName;
  }


  /**
   * Sets the DN attribute name.
   *
   * @param  name  of the DN attribute
   */
  public void setDnAttributeName(final String name)
  {
    dnAttributeName = name;
  }


  /**
   * Returns whether to add the entryDN if an attribute of the same name exists.
   *
   * @return  whether to add the entryDN if an attribute of the same name exists
   */
  public boolean isAddIfExists()
  {
    return addIfExists;
  }


  /**
   * Sets whether to add the entryDN if an attribute of the same name exists.
   *
   * @param  b  whether to add the entryDN if an attribute of the same name exists
   */
  public void setAddIfExists(final boolean b)
  {
    addIfExists = b;
  }


  @Override
  protected void handleAttributes(final Connection conn, final SearchRequest request, final SearchEntry entry)
    throws LdapException
  {
    if (entry.getAttribute(dnAttributeName) == null) {
      entry.addAttribute(new LdapAttribute(dnAttributeName, entry.getDn()));
    } else if (addIfExists) {
      entry.getAttribute(dnAttributeName).addStringValue(entry.getDn());
    }
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, addIfExists, dnAttributeName);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::dnAttributeName=%s, addIfExists=%s]",
        getClass().getName(),
        hashCode(),
        dnAttributeName,
        addIfExists);
  }
}
