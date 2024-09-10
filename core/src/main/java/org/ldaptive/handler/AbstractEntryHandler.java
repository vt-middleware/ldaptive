/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.Set;
import java.util.stream.Collectors;
import org.ldaptive.AbstractFreezable;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for entry handlers which simply returns values unaltered.
 *
 * @author  Middleware Services
 */
public abstract class AbstractEntryHandler extends AbstractFreezable
{

  /** Log for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());


  /**
   * Handle the entry.
   *
   * @param  entry  to handle
   */
  public void handleEntry(final LdapEntry entry)
  {
    entry.setDn(handleDn(entry));
    handleAttributes(entry);
  }


  /**
   * Handle the dn of a search entry.
   *
   * @param  entry  search entry to extract the dn from
   *
   * @return  handled dn
   */
  protected String handleDn(final LdapEntry entry)
  {
    return entry.getDn();
  }


  /**
   * Handle the attributes of a search entry.
   *
   * @param  entry  search entry to extract the attributes from
   */
  protected void handleAttributes(final LdapEntry entry)
  {
    for (LdapAttribute la : entry.getAttributes()) {
      handleAttribute(la);
    }
  }


  /**
   * Handle a single attribute.
   *
   * @param  attr  to handle
   */
  protected void handleAttribute(final LdapAttribute attr)
  {
    if (attr != null) {
      attr.setName(handleAttributeName(attr.getName()));
      // use set semantics to remove potential duplicates introduced by the handler
      if (attr.isBinary()) {
        final Set<byte[]> newValues = attr.getBinaryValues().stream().map(
          this::handleAttributeValue).collect(Collectors.toSet());
        attr.clear();
        attr.addBinaryValues(newValues);
      } else {
        final Set<String> newValues = attr.getStringValues().stream().map(
          this::handleAttributeValue).collect(Collectors.toSet());
        attr.clear();
        attr.addStringValues(newValues);
      }
    }
  }


  /**
   * Returns the supplied attribute name unaltered.
   *
   * @param  name  to handle
   *
   * @return  handled name
   */
  protected String handleAttributeName(final String name)
  {
    return name;
  }


  /**
   * Returns the supplied attribute value unaltered.
   *
   * @param  value  to handle
   *
   * @return  handled value
   */
  protected String handleAttributeValue(final String value)
  {
    return value;
  }


  /**
   * Returns the supplied attribute value unaltered.
   *
   * @param  value  to handle
   *
   * @return  handled value
   */
  protected byte[] handleAttributeValue(final byte[] value)
  {
    return value;
  }


  // CheckStyle:EqualsHashCode OFF
  @Override
  public boolean equals(final Object o)
  {
    return super.equals(o);
  }
  // CheckStyle:EqualsHashCode ON


  @Override
  public abstract int hashCode();
}
