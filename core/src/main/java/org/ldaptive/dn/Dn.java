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
   * Creates a new DN with the supplied string.
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
    if (rdnComponents.size() == 0) {
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
    dn.getRDns().stream().forEach(rdn -> rdnComponents.add(rdn));
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
   * @return  DN with sub-components of this DN
   */
  public Dn subDn(final int beginIndex, final int endIndex)
  {
    return new Dn(IntStream
      .range(0, rdnComponents.size())
      .filter(i -> i >= beginIndex && i < endIndex)
      .mapToObj(i -> rdnComponents.get(i))
      .collect(Collectors.toList()));
  }


  /**
   * Returns the RDN values with the supplied name. If the RDN is multi-value the first value is used.
   *
   * @param  name  of the RDN
   *
   * @return  RDN value
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
   * Produces a string representation of this DN.
   *
   * @return  DN string
   */
  public String format()
  {
    return format(null);
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
    if (rdnComponents == null || rdnComponents.size() == 0) {
      return "";
    }
    if (normalizer == null) {
      return rdnComponents.stream()
        .map(RDn::format)
        .collect(Collectors.joining(","));
    }
    return rdnComponents.stream()
      .map(rdn -> rdn.format(normalizer))
      .collect(Collectors.joining(","));
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
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("rdnComponents=").append(rdnComponents).toString();
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
