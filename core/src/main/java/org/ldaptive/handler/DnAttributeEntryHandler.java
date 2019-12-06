/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;

/**
 * Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020.
 *
 * @author  Middleware Services
 */
public class DnAttributeEntryHandler extends AbstractEntryHandler<LdapEntry> implements LdapEntryHandler
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
  public LdapEntry apply(final LdapEntry entry)
  {
    handleEntry(entry);
    return entry;
  }


  @Override
  protected void handleAttributes(final LdapEntry entry)
  {
    if (entry.getAttribute(dnAttributeName) == null) {
      entry.addAttributes(new LdapAttribute(dnAttributeName, entry.getDn()));
    } else if (addIfExists) {
      entry.getAttribute(dnAttributeName).addStringValues(entry.getDn());
    }
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof DnAttributeEntryHandler) {
      final DnAttributeEntryHandler v = (DnAttributeEntryHandler) o;
      return LdapUtils.areEqual(addIfExists, v.addIfExists) &&
             LdapUtils.areEqual(dnAttributeName, v.dnAttributeName);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, addIfExists, dnAttributeName);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("dnAttributeName=").append(dnAttributeName).append(", ")
      .append("addIfExists=").append(addIfExists).append("]").toString();
  }
}
