/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.protocol.SearchFilter;

/**
 * Presence search filter component defined as:
 *
 * <pre>
 * (attributeDescription=*)
 * </pre>
 *
 * @author  Middleware Services
 */
public class PresenceFilter implements SearchFilter
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10093;

  /** Regex pattern to match this filter type. */
  private static final Pattern FILTER_PATTERN = Pattern.compile("\\((" + ATTRIBUTE_DESC + ")=\\*\\)");

  /** Attribute description. */
  private final String attributeDesc;


  /**
   * Creates a new presence filter.
   *
   * @param  name  attribute description
   */
  public PresenceFilter(final String name)
  {
    attributeDesc = name;
  }


  /**
   * Creates a new presence filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  presence filter or null if component doesn't match this filter type
   */
  public static PresenceFilter parse(final String component)
  {
    final Matcher m = FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      return new PresenceFilter(m.group(1));
    }
    return null;
  }


  @Override
  public DEREncoder getEncoder()
  {
    return new OctetStringType(new ContextDERTag(Type.PRESENCE.ordinal(), false), attributeDesc);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof PresenceFilter) {
      final PresenceFilter v = (PresenceFilter) o;
      return LdapUtils.areEqual(attributeDesc, v.attributeDesc);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, attributeDesc);
  }
}
