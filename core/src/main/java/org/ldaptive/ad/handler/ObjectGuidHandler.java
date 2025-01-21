/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.handler;

import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.ad.GlobalIdentifier;
import org.ldaptive.handler.LdapEntryHandler;

/**
 * Processes an objectGuid attribute by converting it from binary to its string form.
 *
 * @author  Middleware Services
 */
public class ObjectGuidHandler extends AbstractBinaryAttributeHandler<LdapEntry> implements LdapEntryHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1823;

  /** objectGuid attribute name. */
  private static final String ATTRIBUTE_NAME = "objectGUID";

  /** whether to enclose the guid in brackets. */
  private boolean withBrackets = true;

  /** Creates a new object guid handler. */
  public ObjectGuidHandler()
  {
    setAttributeName(ATTRIBUTE_NAME);
  }


  /**
   * Creates a new object guid handler.
   *
   * @param brackets whether to enclose the GUID in brackets
   */
  public ObjectGuidHandler(final boolean brackets)
  {
    setAttributeName(ATTRIBUTE_NAME);
    setWithBrackets(brackets);
  }


  /**
   * Creates a new object guid handler.
   *
   * @param  attrName  name of the attribute which is encoded as an objectGUID
   */
  public ObjectGuidHandler(final String attrName)
  {
    setAttributeName(attrName);
  }


  /**
   * Creates a new object guid handler.
   *
   * @param  attrName  name of the attribute which is encoded as an objectGUID
   * @param brackets whether to enclose the GUID in brackets
   */
  public ObjectGuidHandler(final String attrName, final boolean brackets)
  {
    setAttributeName(attrName);
    setWithBrackets(brackets);
  }


  /**
   * Returns whether the guid will be enclosed in brackets.
   *
   * @return whether the guid will be enclosed in brackets
   */
  public boolean getWithBrackets()
  {
    return withBrackets;
  }


  /**
   * Sets whether to enclose the GUID in brackets.
   *
   * @param brackets whether to enclose the GUID in brackets
   */
  public void setWithBrackets(final boolean brackets)
  {
    assertMutable();
    withBrackets = brackets;
  }


  @Override
  public LdapEntry apply(final LdapEntry entry)
  {
    handleEntry(entry);
    return entry;
  }


  @Override
  protected String convertValue(final byte[] value)
  {
    return GlobalIdentifier.toString(value, withBrackets);
  }


  @Override
  public ObjectGuidHandler newInstance()
  {
    final ObjectGuidHandler handler = new ObjectGuidHandler();
    handler.setAttributeName(getAttributeName());
    handler.setWithBrackets(withBrackets);
    return handler;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof ObjectGuidHandler && super.equals(o)) {
      final ObjectGuidHandler v = (ObjectGuidHandler) o;
      return LdapUtils.areEqual(withBrackets, v.withBrackets);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getAttributeName(), withBrackets);
  }
}
