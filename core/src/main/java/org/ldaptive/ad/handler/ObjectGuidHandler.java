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


  /** Creates a new object guid handler. */
  public ObjectGuidHandler()
  {
    setAttributeName(ATTRIBUTE_NAME);
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


  @Override
  public LdapEntry apply(final LdapEntry entry)
  {
    handleEntry(entry);
    return entry;
  }


  @Override
  protected String convertValue(final byte[] value)
  {
    return GlobalIdentifier.toString(value);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof ObjectGuidHandler && super.equals(o);
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getAttributeName());
  }
}
