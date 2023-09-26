/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.ldaptive.LdapUtils;

/**
 * Normalizes a RDN by performing the following operations:
 * <ul>
 *   <li>lowercase attribute names</li>
 *   <li>lowercase attribute values</li>
 *   <li>compress duplicate spaces in attribute values</li>
 *   <li>escape attribute value characters</li>
 *   <li>sort multi value RDNs by name</li>
 * </ul>
 *
 * This API provides properties to control attribute name normalization, attribute value normalization and attribute
 * value escaping in order to customize the behavior. Note that attribute value normalization occurs before escaping.
 *
 * @author  Middleware Services
 */
public class DefaultRDnNormalizer implements RDnNormalizer
{

  /** Function that returns the value unchanged. */
  public static final Function<String, String> NOOP = new Function<>() {
    @Override
    public String apply(final String s)
    {
      return s;
    }

    @Override
    public String toString()
    {
      return "NOOP";
    }
  };

  /** Function that lowercases the value. */
  public static final Function<String, String> LOWERCASE = new Function<>() {
    @Override
    public String apply(final String s)
    {
      return LdapUtils.toLowerCase(s);
    }

    @Override
    public String toString()
    {
      return "LOWERCASE";
    }
  };

  /** Function that removes duplicate spaces from the value. */
  public static final Function<String, String> COMPRESS = new Function<>() {
    @Override
    public String apply(final String s)
    {
      return LdapUtils.compressSpace(s, false);
    }

    @Override
    public String toString()
    {
      return "COMPRESS";
    }
  };

  /** Function that lowercases and removes duplicate spaces from the value. */
  public static final Function<String, String> LOWERCASE_COMPRESS = new Function<>() {
    @Override
    public String apply(final String s)
    {
      return LdapUtils.toLowerCase(LdapUtils.compressSpace(s, false));
    }

    @Override
    public String toString()
    {
      return "LOWERCASE_COMPRESS";
    }
  };

  /** Attribute name function. */
  private final Function<String, String> attributeNameFunction;

  /** Attribute value function. */
  private final Function<String, String> attributeValueFunction;

  /** Attribute value escaper. */
  private final AttributeValueEscaper attributeValueEscaper;


  /**
   * Creates a new default RDN normalizer.
   */
  public DefaultRDnNormalizer()
  {
    this(new DefaultAttributeValueEscaper(), LOWERCASE, LOWERCASE_COMPRESS);
  }


  /**
   * Creates a new default RDN normalizer.
   *
   * @param  escaper  to escape attribute values
   */
  public DefaultRDnNormalizer(final AttributeValueEscaper escaper)
  {
    this(escaper, LOWERCASE, LOWERCASE_COMPRESS);
  }


  /**
   * Creates a new default RDN normalizer.
   *
   * @param  escaper  to escape attribute values
   * @param  nameNormalizer  to normalize attribute names
   * @param  valueNormalizer  to normalize attribute values
   */
  public DefaultRDnNormalizer(
    final AttributeValueEscaper escaper,
    final Function<String, String> nameNormalizer,
    final Function<String, String> valueNormalizer)
  {
    attributeValueEscaper = escaper;
    attributeNameFunction = nameNormalizer;
    attributeValueFunction = valueNormalizer;
  }


  /**
   * Returns the value escaper.
   *
   * @return  value escaper
   */
  public AttributeValueEscaper getValueEscaper()
  {
    return attributeValueEscaper;
  }


  /**
   * Returns the attribute name function.
   *
   * @return  function for attribute names
   */
  public Function<String, String> getNameFunction()
  {
    return attributeNameFunction;
  }


  /**
   * Returns the attribute value function.
   *
   * @return  function for attribute values
   */
  public Function<String, String> getValueFunction()
  {
    return attributeValueFunction;
  }


  @Override
  public RDn normalize(final RDn rdn)
  {
    final Set<NameValue> nameValues = rdn.getNameValues().stream()
      .map(
        nv -> new NameValue(
          attributeNameFunction.apply(nv.getName()),
          attributeValueEscaper.escape(attributeValueFunction.apply(nv.getStringValue()))))
      .sorted(Comparator.comparing(NameValue::getName))
      .collect(Collectors.toCollection(LinkedHashSet::new));
    return new RDn(nameValues);
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" +
      "attributeNameFunction=" + attributeNameFunction + ", " +
      "attributeValueFunction=" + attributeValueFunction + ", " +
      "attributeValueEscaper=" + attributeValueEscaper;
  }
}
