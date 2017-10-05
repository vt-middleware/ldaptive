/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.handler;

import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.handler.AbstractSearchEntryHandler;

/**
 * Base class for entry handlers that convert a binary attribute to it's string form.
 *
 * @author  Middleware Services
 */
public abstract class AbstractBinaryAttributeHandler extends AbstractSearchEntryHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1847;

  /** attribute name. */
  private String attributeName;


  /**
   * Returns the attribute name to convert from binary to string.
   *
   * @return  attribute name
   */
  public String getAttributeName()
  {
    return attributeName;
  }


  /**
   * Sets the attribute name to convert from binary to string.
   *
   * @param  name  of the attribute
   */
  public void setAttributeName(final String name)
  {
    attributeName = name;
  }


  @Override
  protected void handleAttributes(final Connection conn, final SearchRequest request, final SearchEntry entry)
    throws LdapException
  {
    for (LdapAttribute la : entry.getAttributes()) {
      if (attributeName.equalsIgnoreCase(la.getName())) {
        if (la.isBinary()) {
          final LdapAttribute newAttr = new LdapAttribute(la.getSortBehavior());
          newAttr.setName(la.getName());
          for (byte[] b : la.getBinaryValues()) {
            newAttr.addStringValue(convertValue(b));
          }
          entry.addAttribute(newAttr);
          logger.debug("Processed attribute {}", newAttr);
          handleAttribute(conn, request, newAttr);
        } else {
          logger.warn("Attribute {} must be set as a binary attribute", attributeName);
          handleAttribute(conn, request, la);
        }
      } else {
        handleAttribute(conn, request, la);
      }
    }
  }


  /**
   * Converts the supplied binary value to it's string form.
   *
   * @param  value  to convert
   *
   * @return  string form of the value
   */
  protected abstract String convertValue(byte[] value);


  @Override
  public void initializeRequest(final SearchRequest request)
  {
    boolean isAttrSet = false;
    final String[] binaryAttrs = request.getBinaryAttributes();
    if (binaryAttrs != null) {
      for (String attr : binaryAttrs) {
        if (attributeName.equalsIgnoreCase(attr)) {
          isAttrSet = true;
          break;
        }
      }
      if (!isAttrSet) {
        request.setBinaryAttributes(LdapUtils.concatArrays(binaryAttrs, new String[] {attributeName}));
      }
    } else {
      request.setBinaryAttributes(attributeName);
    }
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof AbstractBinaryAttributeHandler) {
      final AbstractBinaryAttributeHandler v = (AbstractBinaryAttributeHandler) o;
      return LdapUtils.areEqual(attributeName, v.attributeName);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, attributeName);
  }
}
