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
public class DnAttributeEntryHandler extends AbstractEntryHandler implements LdapEntryHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 823;

  /** Attribute name for the entry dn. */
  private String dnAttributeName = "entryDN";

  /**
   * Whether to add the entry dn if an attribute of the same name exists.
   */
  private boolean addIfExists;


  /** Default constructor. */
  public DnAttributeEntryHandler() {}


  /**
   * Creates a new DN attribute entry handler.
   *
   * @param  attrName  to add the DN to
   * @param  add  whether to add the DN if the attribute exists
   */
  public DnAttributeEntryHandler(final String attrName, final boolean add)
  {
    dnAttributeName = attrName;
    addIfExists = add;
  }


  /**
   * Returns the DN attribute name.
   *
   * @return  DN attribute name
   */
  public final String getDnAttributeName()
  {
    return dnAttributeName;
  }


  /**
   * Sets the DN attribute name.
   *
   * @param  name  of the DN attribute
   */
  public final void setDnAttributeName(final String name)
  {
    assertMutable();
    dnAttributeName = name;
  }


  /**
   * Returns whether to add the entryDN if an attribute of the same name exists.
   *
   * @return  whether to add the entryDN if an attribute of the same name exists
   */
  public final boolean isAddIfExists()
  {
    return addIfExists;
  }


  /**
   * Sets whether to add the entryDN if an attribute of the same name exists.
   *
   * @param  b  whether to add the entryDN if an attribute of the same name exists
   */
  public final void setAddIfExists(final boolean b)
  {
    assertMutable();
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
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "dnAttributeName=" + dnAttributeName + ", " +
      "addIfExists=" + addIfExists + "]";
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  /** DN attribute entry handler builder. */
  public static final class Builder
  {

    /** DN attribute entry handler to build. */
    private final DnAttributeEntryHandler object = new DnAttributeEntryHandler();


    /**
     * Default constructor.
     */
    private Builder() {}


    /**
     * Makes this instance immutable.
     *
     * @return  this builder
     */
    public Builder freeze()
    {
      object.freeze();
      return this;
    }


    /**
     * Sets the DN attribute name.
     *
     * @param  name  DN attribute name
     *
     * @return  this builder
     */
    public Builder dnAttributeName(final String name)
    {
      object.setDnAttributeName(name);
      return this;
    }


    /**
     * Sets the add if exists.
     *
     * @param  b  add if exists
     *
     * @return  this builder
     */
    public Builder addIfExists(final boolean b)
    {
      object.setAddIfExists(b);
      return this;
    }


    /**
     * Returns the DN attribute entry handler.
     *
     * @return  DN attribute entry handler
     */
    public DnAttributeEntryHandler build()
    {
      return object;
    }
  }
}
