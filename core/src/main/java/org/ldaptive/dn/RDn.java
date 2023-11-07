/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.ldaptive.LdapUtils;

/**
 * Relative distinguished name containing one or more name value pairs. Name value pairs are ordered from left to right
 * such that the left-most pair is considered the first. For the RDN 'cn=Jane Doe+mail=jdoe@example.com', the first name
 * value pair is 'cn=Jane Doe'.
 *
 * See <a href="http://www.ietf.org/rfc/rfc4514.txt">RFC 4514</a> for more details on the string representations of
 * RDNs.
 *
 * @author  Middleware Services
 */
public class RDn
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 5009;

  /** Name value pairs. */
  private final Set<NameValue> nameValues;


  /**
   * Creates a new RDN with the supplied string.
   *
   * @param  rdn  to parse
   *
   * @throws  IllegalArgumentException  if rdn contains multiple RDNs or no RDNs
   */
  public RDn(final String rdn)
  {
    this(rdn, new DefaultDnParser());
  }


  /**
   * Creates a new RDN with the supplied string.
   *
   * @param  rdn  to parse
   * @param  parser  to parse dn
   *
   * @throws  IllegalArgumentException  if rdn contains multiple RDNs or no RDNS
   */
  public RDn(final String rdn, final DnParser parser)
  {
    final List<RDn> rdns = parser.parse(rdn);
    if (rdns.isEmpty()) {
      throw new IllegalArgumentException("Invalid RDN: no RDNs found in " + rdn);
    }
    if (rdns.size() > 1) {
      throw new IllegalArgumentException("Invalid RDN: multiple RDNs found in " + rdn);
    }
    nameValues = Collections.unmodifiableSet(rdns.get(0).getNameValues());
  }


  /**
   * Creates a new RDN with the supplied name value pairs.
   *
   * @param  value  to add
   */
  public RDn(final NameValue... value)
  {
    nameValues = Stream.of(value).filter(Objects::nonNull).collect(
      Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
  }


  /**
   * Creates a new RDN with the supplied name value pairs.
   *
   * @param  values  to add
   */
  public RDn(final Collection<NameValue> values)
  {
    nameValues = values.stream().filter(Objects::nonNull).collect(
      Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
  }


  /**
   * Creates a new RDN with a single name value pair.
   *
   * @param  attributeName  to add
   * @param  attributeValue  to add
   */
  public RDn(final String attributeName, final String attributeValue)
  {
    nameValues = Set.of(new NameValue(attributeName, attributeValue));
  }


  /**
   * Returns the first name value pair in this RDN.
   *
   * @return  name value pair
   */
  public NameValue getNameValue()
  {
    if (nameValues.isEmpty()) {
      return null;
    }
    return nameValues.iterator().next();
  }


  /**
   * Returns all the name value pairs in this RDN.
   *
   * @return  name value paris
   */
  public Set<NameValue> getNameValues()
  {
    return nameValues;
  }


  /**
   * Returns all the names in this RDN.
   *
   * @return  all names
   */
  public List<String> getNames()
  {
    return nameValues.stream().map(NameValue::getName).collect(Collectors.toUnmodifiableList());
  }


  /**
   * Returns the name values that match the supplied name.
   *
   * @param  name  to match
   *
   * @return  name values
   */
  public Set<NameValue> getNameValues(final String name)
  {
    return nameValues.stream().filter(nv -> nv.hasName(name))
      .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
  }


  /**
   * Returns a single name value that matches the supplied name. See {@link #getNameValues(String)}.
   *
   * @param  name  to match
   *
   * @return  name value
   */
  public NameValue getNameValue(final String name)
  {
    return getNameValues(name).stream().findFirst().orElse(null);
  }


  /**
   * Returns the number of name value pairs in this RDN.
   *
   * @return  RDN size
   */
  public int size()
  {
    return nameValues.size();
  }


  /**
   * Returns whether this RDN contains any name values.
   *
   * @return  whether this RDN contains any name values
   */
  public boolean isEmpty()
  {
    return nameValues.isEmpty();
  }


  /**
   * Returns whether the normalized format of the supplied RDN equals the normalized format of this RDN. See {@link
   * DefaultRDnNormalizer}.
   *
   * @param  rdn  to compare
   *
   * @return  whether the supplied RDN is the same as this RDN
   */
  public boolean isSame(final RDn rdn)
  {
    return isSame(rdn, new DefaultRDnNormalizer());
  }


  /**
   * Returns whether the normalized format of the supplied RDN equals the normalized format of this RDN.
   *
   * @param  normalizer  to use for comparison
   * @param  rdn  to compare
   *
   * @return  whether the supplied RDN is the same as this RDN
   */
  public boolean isSame(final RDn rdn, final RDnNormalizer normalizer)
  {
    return format(normalizer).equals(rdn.format(normalizer));
  }


  /**
   * Returns a string representation of this RDN. Uses a {@link DefaultRDnNormalizer} by default.
   *
   * @return  string form of the RDN
   */
  public String format()
  {
    return format(new DefaultRDnNormalizer());
  }


  /**
   * Returns a string representation of this RDN, joining each name value pair with '+'.
   *
   * @param  normalizer  to apply to the RDN components or null for no formatting
   *
   * @return  string form of the RDN
   */
  public String format(final RDnNormalizer normalizer)
  {
    final String formatted;
    switch (nameValues.size()) {
    case 0:
      return "";
    case 1:
      if (normalizer != null) {
        formatted = normalizer.normalize(this).getNameValues().iterator().next().format();
      } else {
        formatted = nameValues.iterator().next().format();
      }
      break;
    default:
      if (normalizer != null) {
        formatted = normalizer.normalize(this).getNameValues().stream()
          .map(NameValue::format)
          .collect(Collectors.joining("+"));
      } else {
        formatted = nameValues.stream().map(NameValue::format).collect(Collectors.joining("+"));
      }
    }
    return formatted;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof RDn) {
      final RDn v = (RDn) o;
      return LdapUtils.areEqual(nameValues, v.nameValues);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, nameValues);
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" + "nameValues=" + nameValues;
  }
}
