/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Normalizes a RDN by performing the following operations:
 * <ul>
 *   <li>lowercase attribute names</li>
 *   <li>escape attribute value characters</li>
 *   <li>sort multi value RDNs by name</li>
 * </ul>
 * @author  Middleware Services
 */
public class DefaultRDnNormalizer implements RDnNormalizer
{

  /** Value escaper. */
  private final AttributeValueEscaper valueEscaper;


  /**
   * Creates a new default RDN normalizer.
   */
  public DefaultRDnNormalizer()
  {
    this(new DefaultAttributeValueEscaper());
  }


  /**
   * Creates a new default RDN normalizer.
   *
   * @param  escaper  to escape attribute values
   */
  public DefaultRDnNormalizer(final AttributeValueEscaper escaper)
  {
    valueEscaper = escaper;
  }


  /**
   * Returns the value escaper.
   *
   * @return  value escaper
   */
  public AttributeValueEscaper getValueEscaper()
  {
    return valueEscaper;
  }


  @Override
  public RDn normalize(final RDn rdn)
  {
    final Set<NameValue> nameValues = rdn.getNameValues().stream()
      .map(nv -> new NameValue(normalizeName(nv.getAttributeName()), normalizeValue(nv.getAttributeValue())))
      .sorted(Comparator.comparing(nv -> nv.getAttributeName()))
      .collect(Collectors.toCollection(LinkedHashSet::new));
    return new RDn(nameValues);
  }


  /**
   * Lower cases the supplied name.
   *
   * @param  name  to normalize
   *
   * @return  normalized name
   */
  private String normalizeName(final String name)
  {
    return name.toLowerCase();
  }


  /**
   * Escapes the supplied value.
   *
   * @param  value  to normalize
   *
   * @return  normalized value
   */
  private String normalizeValue(final String value)
  {
    return valueEscaper.escape(value);
  }
}
