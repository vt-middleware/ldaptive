/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.ldaptive.LdapUtils;

/**
 * Distinguished name containing zero or more relative distinguished names. RDNs are ordered from left to right such
 * that the left-most RDN is considered the first. For the DN 'cn=Jane Doe,ou=People,dc=ldaptive,dc=org', the first RDN
 * is 'cn=Jane Doe'.
 *
 * See <a href="http://www.ietf.org/rfc/rfc4514.txt">RFC 4514</a> for more details on the string representations of
 * DNs.
 *
 * @author  Middleware Services
 */
public class Dn
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 5003;

  /** RDN components. */
  private final List<RDn> rdnComponents = new ArrayList<>();


  /** Default constructor. */
  public Dn() {}


  /**
   * Creates a new DN with the supplied string. Uses a {@link DefaultDnParser} by default.
   *
   * @param  dn  to parse
   */
  public Dn(final String dn)
  {
    this(dn, new DefaultDnParser());
  }


  /**
   * Creates a new DN with the supplied string.
   *
   * @param  dn  to parse
   * @param  parser  to parse dn
   */
  public Dn(final String dn, final DnParser parser)
  {
    rdnComponents.addAll(parser.parse(dn));
  }


  /**
   * Creates a new DN with the supplied RDNs.
   *
   * @param  rdn  to add
   */
  public Dn(final RDn... rdn)
  {
    this(Arrays.asList(rdn));
  }


  /**
   * Creates a new DN with the supplied RDNs.
   *
   * @param  rdns  to add
   */
  public Dn(final List<RDn> rdns)
  {
    rdnComponents.addAll(rdns);
  }


  /**
   * Returns the first RDN in this DN.
   *
   * @return  first RDN
   */
  public RDn getRDn()
  {
    if (rdnComponents.isEmpty()) {
      return null;
    }
    return rdnComponents.get(0);
  }


  /**
   * Returns the RDNs in this DN.
   *
   * @return  RDNs
   */
  public List<RDn> getRDns()
  {
    return rdnComponents;
  }


  /**
   * Adds all the RDNs in the supplied DN to the end of this DN.
   *
   * @param  dn  to add to this DN
   */
  public void add(final Dn dn)
  {
    rdnComponents.addAll(dn.getRDns());
  }


  /**
   * Adds the supplied RDN to the end of this DN.
   *
   * @param  rdn  to add to this DN
   */
  public void add(final RDn rdn)
  {
    rdnComponents.add(rdn);
  }


  /**
   * Adds the supplied RDN at the supplied index.
   *
   * @param  index  to add the RDN at
   * @param  rdn  to add to this DN
   */
  public void add(final int index, final RDn rdn)
  {
    rdnComponents.add(index, rdn);
  }


  /**
   * Returns a new DN containing all the RDN components from the supplied index.
   *
   * @param  index  of RDNs to include
   *
   * @return  DN with sub-components of this DN
   */
  public Dn subDn(final int index)
  {
    return subDn(index, rdnComponents.size());
  }


  /**
   * Returns a new DN containing all the RDN components between the supplied indexes.
   *
   * @param  beginIndex  first RDN to include (inclusive)
   * @param  endIndex  last RDN to include (exclusive)
   *
   * @return  DN with sub-components of this DN or null if beginIndex &gt; endIndex
   */
  public Dn subDn(final int beginIndex, final int endIndex)
  {
    if (beginIndex > endIndex) {
      return null;
    }
    return new Dn(IntStream
      .range(0, rdnComponents.size())
      .filter(i -> i >= beginIndex && i < endIndex)
      .mapToObj(rdnComponents::get)
      .collect(Collectors.toList()));
  }


  /**
   * Convenience method to retrieve the parent DN. Invokes {@link #subDn(int)} with a parameter of 1.
   *
   * @return  DN containing all sub-components of this DN except the first or null if this DN has no components
   */
  public Dn getParent()
  {
    return subDn(1);
  }


  /**
   * Returns all the RDN names.
   *
   * @return  all RDN names
   */
  public Collection<String> getNames()
  {
    return rdnComponents.stream()
      .flatMap(rdn -> rdn.getNames().stream())
      .collect(Collectors.toList());
  }


  /**
   * Returns the RDN values with the supplied name. If the RDN is multi-value the first value is used.
   *
   * @param  name  of the RDN
   *
   * @return  RDN values for the supplied name
   */
  public Collection<String> getValues(final String name)
  {
    return rdnComponents.stream()
      .filter(rdn -> rdn.getNameValue().hasName(name))
      .map(rdn -> rdn.getNameValue().getStringValue())
      .collect(Collectors.toList());
  }


  /**
   * Returns the first RDN value with the supplied name. If the RDN is multi-value the first value is used.
   *
   * @param  name  of the RDN
   *
   * @return  RDN value
   */
  public String getValue(final String name)
  {
    return getValues(name).stream().findFirst().orElse(null);
  }


  /**
   * Returns the number of RDNs in this DN.
   *
   * @return  number of RDNs
   */
  public int size()
  {
    return rdnComponents.size();
  }


  /**
   * Returns whether this DN contains any RDN components.
   *
   * @return  whether this DN contains any RDN components
   */
  public boolean isEmpty()
  {
    return rdnComponents.isEmpty();
  }


  /**
   * Returns whether the normalized format of the supplied DN equals the normalized format of this DN. See {@link
   * DefaultRDnNormalizer}.
   *
   * @param  dn  to compare
   *
   * @return  whether the supplied DN is the same as this DN
   */
  public boolean isSame(final Dn dn)
  {
    return isSame(dn, new DefaultRDnNormalizer());
  }


  /**
   * Returns whether the normalized format of the supplied DN equals the normalized format of this DN.
   *
   * @param  normalizer  to use for comparison
   * @param  dn  to compare
   *
   * @return  whether the supplied DN is the same as this DN
   */
  public boolean isSame(final Dn dn, final RDnNormalizer normalizer)
  {
    return format(normalizer).equals(dn.format(normalizer));
  }


  /**
   * Returns whether the supplied DN is an ancestor. See {@link #isSame(Dn)}.
   *
   * @param  dn  to determine ancestry of
   *
   * @return  whether the supplied DN is an ancestor
   */
  public boolean isAncestor(final Dn dn)
  {
    return isAncestor(dn, new DefaultRDnNormalizer());
  }


  /**
   * Returns whether the supplied DN is an ancestor. See {@link #isSame(Dn, RDnNormalizer)}.
   *
   * @param  dn  to determine ancestry of
   * @param  normalizer  to format DN for comparison
   *
   * @return  whether the supplied DN is an ancestor
   */
  public boolean isAncestor(final Dn dn, final RDnNormalizer normalizer)
  {
    // all DNs are ancestors from the root DN
    if (isEmpty() && !dn.isEmpty()) {
      return true;
    }

    // greater than or equal number of RDNs, cannot be an ancestor
    if (size() >= dn.size()) {
      return false;
    }

    int index = size() - 1;
    int dnIndex = dn.size() - 1;
    boolean ancestor = true;
    while (index >= 0) {
      if (!getRDns().get(index--).isSame(dn.getRDns().get(dnIndex--), normalizer)) {
        ancestor = false;
        break;
      }
    }
    return ancestor;
  }


  /**
   * Returns whether the supplied DN is a descendant. See {@link #isSame(Dn)}.
   *
   * @param  dn  to determine descendancy of
   *
   * @return  whether the supplied DN is a descendant
   */
  public boolean isDescendant(final Dn dn)
  {
    return isDescendant(dn, new DefaultRDnNormalizer());
  }


  /**
   * Returns whether the supplied DN is a descendant. See {@link #isSame(Dn, RDnNormalizer)}.
   *
   * @param  dn  to determine descendancy of
   * @param  normalizer  to format DN for comparison
   *
   * @return  whether the supplied DN is a descendant
   */
  public boolean isDescendant(final Dn dn, final RDnNormalizer normalizer)
  {
    return dn.isAncestor(this, normalizer);
  }


  /**
   * Produces a string representation of this DN. Uses a {@link DefaultRDnNormalizer} by default.
   *
   * @return  DN string
   */
  public String format()
  {
    return format(new DefaultRDnNormalizer());
  }


  /**
   * Produces a string representation of this DN.
   *
   * @param  normalizer  to apply to the RDN components or null for no formatting
   *
   * @return  DN string
   */
  public String format(final RDnNormalizer normalizer)
  {
    return format(normalizer, ',', false);
  }


  /**
   * Produces a string representation of this DN.
   *
   * @param  normalizer  to apply to the RDN components or null for no formatting
   * @param  delimiter  to separate each RDN component
   * @param  reverse  whether to reverse the order of RDN components for formatting.
   *                  i.e. process components from right to left
   *
   * @return  DN string
   */
  public String format(final RDnNormalizer normalizer, final char delimiter, final boolean reverse)
  {
    if (rdnComponents.isEmpty()) {
      return "";
    }
    final StringBuilder sb = new StringBuilder();
    if (reverse) {
      for (int i = rdnComponents.size() - 1; i >= 0; i--) {
        sb.append(rdnComponents.get(i).format(normalizer)).append(delimiter);
      }
    } else {
      for (RDn rdnComponent : rdnComponents) {
        sb.append(rdnComponent.format(normalizer)).append(delimiter);
      }
    }
    if (sb.length() > 0 && sb.charAt(sb.length() - 1) == delimiter) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof Dn) {
      final Dn v = (Dn) o;
      return LdapUtils.areEqual(rdnComponents, v.rdnComponents);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, rdnComponents);
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" + "rdnComponents=" + rdnComponents;
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Dn.Builder builder()
  {
    return new Dn.Builder();
  }


  // CheckStyle:OFF
  public static class Builder
  {


    private final Dn object = new Dn();


    protected Builder() {}


    public Dn.Builder add(final String dn)
    {
      object.add(new Dn(dn));
      return this;
    }


    public Dn.Builder add(final Dn dn)
    {
      object.add(dn);
      return this;
    }


    public Dn.Builder add(final RDn rdn)
    {
      object.add(rdn);
      return this;
    }


    public Dn build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
