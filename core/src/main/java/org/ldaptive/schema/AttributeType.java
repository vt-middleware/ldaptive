/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Bean for an attribute type schema element.
 *
 * <pre>
   AttributeTypeDescription = LPAREN WSP
     numericoid                    ; object identifier
     [ SP "NAME" SP qdescrs ]      ; short names (descriptors)
     [ SP "DESC" SP qdstring ]     ; description
     [ SP "OBSOLETE" ]             ; not active
     [ SP "SUP" SP oid ]           ; supertype
     [ SP "EQUALITY" SP oid ]      ; equality matching rule
     [ SP "ORDERING" SP oid ]      ; ordering matching rule
     [ SP "SUBSTR" SP oid ]        ; substrings matching rule
     [ SP "SYNTAX" SP noidlen ]    ; value syntax
     [ SP "SINGLE-VALUE" ]         ; single-value
     [ SP "COLLECTIVE" ]           ; collective
     [ SP "NO-USER-MODIFICATION" ] ; not user modifiable
     [ SP "USAGE" SP usage ]       ; usage
     extensions WSP RPAREN         ; extensions
 * </pre>
 *
 * @author  Middleware Services
 */
public final class AttributeType extends AbstractNamedSchemaElement
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1103;

  /** OID. */
  private final String oid;

  /** Superior type. */
  private String superiorType;

  /** Equality matching rule. */
  private String equalityMatchingRule;

  /** Ordering matching rule. */
  private String orderingMatchingRule;

  /** Substring matching rule. */
  private String substringMatchingRule;

  /** Syntax OID. */
  private String syntaxOID;

  /** Single valued. */
  private boolean singleValued;

  /** Collective. */
  private boolean collective;

  /** No user modification. */
  private boolean noUserModification;

  /** Usage. */
  private AttributeUsage usage;


  /**
   * Creates a new attribute type.
   *
   * @param  s  oid
   */
  public AttributeType(final String s)
  {
    oid = s;
  }


  /**
   * Creates a new attribute type.
   *
   * @param  oid  oid
   * @param  names  names
   * @param  description  description
   * @param  obsolete  obsolete
   * @param  superiorType  superior type
   * @param  equalityMatchingRule  equality matching rule
   * @param  orderingMatchingRule  ordering matching rule
   * @param  substringMatchingRule  substring matching rule
   * @param  syntaxOID  syntax OID
   * @param  singleValued  single valued
   * @param  collective  collective
   * @param  noUserModification  no user modification
   * @param  usage  usage
   * @param  extensions  extensions
   */
  // CheckStyle:ParameterNumber|HiddenField OFF
  public AttributeType(
    final String oid,
    final String[] names,
    final String description,
    final boolean obsolete,
    final String superiorType,
    final String equalityMatchingRule,
    final String orderingMatchingRule,
    final String substringMatchingRule,
    final String syntaxOID,
    final boolean singleValued,
    final boolean collective,
    final boolean noUserModification,
    final AttributeUsage usage,
    final Extensions extensions)
  {
    this(oid);
    setNames(names);
    setDescription(description);
    setObsolete(obsolete);
    setSuperiorType(superiorType);
    setEqualityMatchingRule(equalityMatchingRule);
    setOrderingMatchingRule(orderingMatchingRule);
    setSubstringMatchingRule(substringMatchingRule);
    setSyntaxOID(syntaxOID);
    setSingleValued(singleValued);
    setCollective(collective);
    setNoUserModification(noUserModification);
    setUsage(usage);
    setExtensions(extensions);
  }
  // CheckStyle:ParameterNumber|HiddenField ON


  /**
   * Returns the oid.
   *
   * @return  oid
   */
  public String getOID()
  {
    return oid;
  }


  /**
   * Returns the superior type.
   *
   * @return  superior type
   */
  public String getSuperiorType()
  {
    return superiorType;
  }


  /**
   * Sets the superior type.
   *
   * @param  s  superior type
   */
  public void setSuperiorType(final String s)
  {
    assertMutable();
    superiorType = s;
  }


  /**
   * Returns the equality matching rule.
   *
   * @return  equality matching rule
   */
  public String getEqualityMatchingRule()
  {
    return equalityMatchingRule;
  }


  /**
   * Sets the equality matching rule.
   *
   * @param  s  equality matching rule
   */
  public void setEqualityMatchingRule(final String s)
  {
    assertMutable();
    equalityMatchingRule = s;
  }


  /**
   * Returns the ordering matching rule.
   *
   * @return  ordering matching rule
   */
  public String getOrderingMatchingRule()
  {
    return orderingMatchingRule;
  }


  /**
   * Sets the ordering matching rule.
   *
   * @param  s  ordering matching rule
   */
  public void setOrderingMatchingRule(final String s)
  {
    assertMutable();
    orderingMatchingRule = s;
  }


  /**
   * Returns the substring matching rule.
   *
   * @return  substring matching rule
   */
  public String getSubstringMatchingRule()
  {
    return substringMatchingRule;
  }


  /**
   * Sets the substring matching rule.
   *
   * @param  s  substring matching rule
   */
  public void setSubstringMatchingRule(final String s)
  {
    assertMutable();
    substringMatchingRule = s;
  }


  /**
   * Returns the syntax oid.
   *
   * @return  syntax oid
   */
  public String getSyntaxOID()
  {
    return syntaxOID;
  }


  /**
   * Returns the syntax oid.
   *
   * @param  withBoundCount  whether the bound count should be included
   *
   * @return  syntax oid
   */
  public String getSyntaxOID(final boolean withBoundCount)
  {
    if (!withBoundCount && syntaxOID != null && syntaxOID.contains("{") && syntaxOID.endsWith("}")) {
      return syntaxOID.substring(0, syntaxOID.indexOf('{'));
    }
    return syntaxOID;
  }


  /**
   * Returns the syntax oid bound count.
   *
   * @return  syntax oid bound count
   */
  public int getSyntaxOIDBoundCount()
  {
    if (syntaxOID != null && syntaxOID.contains("{") && syntaxOID.endsWith("}")) {
      final String count = syntaxOID.substring(syntaxOID.indexOf('{') + 1, syntaxOID.length() - 1);
      return Integer.parseInt(count);
    }
    return -1;
  }


  /**
   * Sets the syntax oid.
   *
   * @param  s  syntax oid
   */
  public void setSyntaxOID(final String s)
  {
    assertMutable();
    syntaxOID = s;
  }


  /**
   * Returns whether this attribute type is single valued.
   *
   * @return  whether this attribute type is single valued
   */
  public boolean isSingleValued()
  {
    return singleValued;
  }


  /**
   * Sets whether this attribute type is single valued.
   *
   * @param  b  whether this attribute type is single valued
   */
  public void setSingleValued(final boolean b)
  {
    assertMutable();
    singleValued = b;
  }


  /**
   * Returns whether this attribute type is collective.
   *
   * @return  whether this attribute type is collective
   */
  public boolean isCollective()
  {
    return collective;
  }


  /**
   * Sets whether this attribute type is collective.
   *
   * @param  b  whether this attribute type is collective
   */
  public void setCollective(final boolean b)
  {
    assertMutable();
    collective = b;
  }


  /**
   * Returns whether this attribute type allows user modification.
   *
   * @return  whether this attribute type allows user modification
   */
  public boolean isNoUserModification()
  {
    return noUserModification;
  }


  /**
   * Sets whether this attribute type allows user modification.
   *
   * @param  b  whether this attribute type allows user modification
   */
  public void setNoUserModification(final boolean b)
  {
    assertMutable();
    noUserModification = b;
  }


  /**
   * Returns the usage.
   *
   * @return  usage
   */
  public AttributeUsage getUsage()
  {
    return usage != null ? usage : AttributeUsage.USER_APPLICATIONS;
  }


  /**
   * Sets the usage.
   *
   * @param  u  attribute usage
   */
  public void setUsage(final AttributeUsage u)
  {
    assertMutable();
    usage = u;
  }


  /**
   * Parses the supplied definition string and creates an initialized attribute type.
   *
   * @param  definition  to parse
   *
   * @return  attribute type
   *
   * @throws  SchemaParseException  if the supplied definition is invalid
   */
  public static AttributeType parse(final String definition)
    throws SchemaParseException
  {
    return SchemaParser.parse(AttributeType.class, definition);
  }


  @Override
  public String format()
  {
    final StringBuilder sb = new StringBuilder("( ");
    sb.append(oid).append(" ");
    if (getNames() != null && getNames().length > 0) {
      sb.append("NAME ").append(SchemaUtils.formatDescriptors(getNames()));
    }
    if (getDescription() != null) {
      sb.append("DESC ").append(SchemaUtils.formatDescriptors(getDescription()));
    }
    if (isObsolete()) {
      sb.append("OBSOLETE ");
    }
    if (superiorType != null) {
      sb.append("SUP ").append(superiorType).append(" ");
    }
    if (equalityMatchingRule != null) {
      sb.append("EQUALITY ").append(equalityMatchingRule).append(" ");
    }
    if (orderingMatchingRule != null) {
      sb.append("ORDERING ").append(orderingMatchingRule).append(" ");
    }
    if (substringMatchingRule != null) {
      sb.append("SUBSTR ").append(substringMatchingRule).append(" ");
    }
    if (syntaxOID != null) {
      sb.append("SYNTAX ").append(syntaxOID).append(" ");
    }
    if (singleValued) {
      sb.append("SINGLE-VALUE ");
    }
    if (collective) {
      sb.append("COLLECTIVE ");
    }
    if (noUserModification) {
      sb.append("NO-USER-MODIFICATION ");
    }
    if (usage != null) {
      sb.append("USAGE ").append(usage.getName()).append(" ");
    }
    if (getExtensions() != null) {
      sb.append(getExtensions().format());
    }
    sb.append(")");
    return sb.toString();
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof AttributeType) {
      final AttributeType v = (AttributeType) o;
      return LdapUtils.areEqual(oid, v.oid) &&
             LdapUtils.areEqual(getNames(), v.getNames()) &&
             LdapUtils.areEqual(getDescription(), v.getDescription()) &&
             LdapUtils.areEqual(isObsolete(), v.isObsolete()) &&
             LdapUtils.areEqual(superiorType, v.superiorType) &&
             LdapUtils.areEqual(equalityMatchingRule, v.equalityMatchingRule) &&
             LdapUtils.areEqual(orderingMatchingRule, v.orderingMatchingRule) &&
             LdapUtils.areEqual(substringMatchingRule, v.substringMatchingRule) &&
             LdapUtils.areEqual(syntaxOID, v.syntaxOID) &&
             LdapUtils.areEqual(singleValued, v.singleValued) &&
             LdapUtils.areEqual(collective, v.collective) &&
             LdapUtils.areEqual(noUserModification, v.noUserModification) &&
             LdapUtils.areEqual(usage, v.usage) &&
             LdapUtils.areEqual(getExtensions(), v.getExtensions());
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        oid,
        getNames(),
        getDescription(),
        isObsolete(),
        superiorType,
        equalityMatchingRule,
        orderingMatchingRule,
        substringMatchingRule,
        syntaxOID,
        singleValued,
        collective,
        noUserModification,
        usage,
        getExtensions());
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "oid=" + oid + ", " +
      "names=" + Arrays.toString(getNames()) + ", " +
      "description=" + getDescription() + ", " +
      "obsolete=" + isObsolete() + ", " +
      "superiorType=" + superiorType + ", " +
      "equalityMatchingRule=" + equalityMatchingRule + ", " +
      "orderingMatchingRule=" + orderingMatchingRule + ", " +
      "substringMatchingRule=" + substringMatchingRule + ", " +
      "syntaxOID=" + syntaxOID + ", " +
      "singleValued=" + singleValued + ", " +
      "collective=" + collective + ", " +
      "noUserModification=" + noUserModification + ", " +
      "usage=" + usage + ", " +
      "extensions=" + getExtensions() + "]";
  }


  /** Parses an attribute type definition using a char buffer. */
  public static class DefaultDefinitionFunction extends AbstractDefaultDefinitionFunction<AttributeType>
  {


    @Override
    public AttributeType parse(final String definition)
      throws SchemaParseException
    {
      final CharBuffer buffer = validate(definition);
      skipSpaces(buffer);
      final AttributeType atd = new AttributeType(readUntilSpace(buffer));
      final Extensions exts = new Extensions();
      while (buffer.hasRemaining()) {
        skipSpaces(buffer);
        final String token = readUntilSpace(buffer);
        skipSpaces(buffer);
        switch (token) {
        case "NAME":
          atd.setNames(readQDStrings(buffer));
          break;
        case "DESC":
          atd.setDescription(readQDString(buffer));
          break;
        case "OBSOLETE":
          atd.setObsolete(true);
          break;
        case "SUP":
          atd.setSuperiorType(readOID(buffer));
          break;
        case "EQUALITY":
          atd.setEqualityMatchingRule(readOID(buffer));
          break;
        case "ORDERING":
          atd.setOrderingMatchingRule(readOID(buffer));
          break;
        case "SUBSTR":
          atd.setSubstringMatchingRule(readOID(buffer));
          break;
        case "SYNTAX":
          atd.setSyntaxOID(readUntilSpace(buffer));
          break;
        case "SINGLE-VALUE":
          atd.setSingleValued(true);
          break;
        case "COLLECTIVE":
          atd.setCollective(true);
          break;
        case "NO-USER-MODIFICATION":
          atd.setNoUserModification(true);
          break;
        case "USAGE":
          atd.setUsage(AttributeUsage.parse(readUntilSpace(buffer)));
          break;
        case "":
          break;
        default:
          if (!token.startsWith("X-")) {
            throw new SchemaParseException(
              "Definition '" + definition + "' contains invalid extension '" + token + "'");
          }
          skipSpaces(buffer);
          exts.addExtension(token, List.of(readQDStrings(buffer)));
          break;
        }
      }
      if (!exts.isEmpty()) {
        atd.setExtensions(exts);
      }
      return atd;
    }
  }


  /** Parses an attribute type definition using a regular expression. */
  public static class RegexDefinitionFunction extends AbstractRegexDefinitionFunction<AttributeType>
  {

    /** Pattern to match definitions. */
    private static final Pattern DEFINITION_PATTERN = Pattern.compile(
      WSP_REGEX + "\\(" +
        WSP_REGEX + "(" + NO_WSP_REGEX + ")" +
        WSP_REGEX + "(?:NAME" + ONE_WSP_REGEX + "(?:'([^']+)'|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:DESC" + ONE_WSP_REGEX + "'([^']*)')?" +
        WSP_REGEX + "(OBSOLETE)?" +
        WSP_REGEX + "(?:SUP" + ONE_WSP_REGEX + "(" + NO_WSP_REGEX + "))?" +
        WSP_REGEX + "(?:EQUALITY" + ONE_WSP_REGEX + "(" + NO_WSP_REGEX + "))?" +
        WSP_REGEX + "(?:ORDERING" + ONE_WSP_REGEX + "(" + NO_WSP_REGEX + "))?" +
        WSP_REGEX + "(?:SUBSTR" + ONE_WSP_REGEX + "(" + NO_WSP_REGEX + "))?" +
        WSP_REGEX + "(?:SYNTAX" + ONE_WSP_REGEX + "(" + NO_WSP_REGEX + "))?" +
        WSP_REGEX + "(SINGLE-VALUE)?" +
        WSP_REGEX + "(COLLECTIVE)?" +
        WSP_REGEX + "(NO-USER-MODIFICATION)?" +
        WSP_REGEX + "(?:USAGE" + ONE_WSP_REGEX + "(\\p{Alpha}+))?" +
        WSP_REGEX + "(?:(X-[^ ]+.*))?" +
        WSP_REGEX + "\\)" + WSP_REGEX);


    @Override
    public AttributeType parse(final String definition)
      throws SchemaParseException
    {
      final Matcher m = DEFINITION_PATTERN.matcher(definition);
      if (!m.matches()) {
        throw new SchemaParseException("Invalid attribute type definition: " + definition);
      }

      final AttributeType atd = new AttributeType(m.group(1).trim());

      // CheckStyle:MagicNumber OFF
      // parse names
      if (m.group(2) != null) {
        atd.setNames(SchemaUtils.parseDescriptors(m.group(2).trim()));
      } else if (m.group(3) != null) {
        atd.setNames(SchemaUtils.parseDescriptors(m.group(3).trim()));
      }

      atd.setDescription(m.group(4) != null ? m.group(4).trim() : null);
      atd.setObsolete(m.group(5) != null);
      atd.setSuperiorType(m.group(6) != null ? m.group(6).trim() : null);
      atd.setEqualityMatchingRule(m.group(7) != null ? m.group(7).trim() : null);
      atd.setOrderingMatchingRule(m.group(8) != null ? m.group(8).trim() : null);
      atd.setSubstringMatchingRule(m.group(9) != null ? m.group(9).trim() : null);
      atd.setSyntaxOID(m.group(10) != null ? m.group(10).trim() : null);
      atd.setSingleValued(m.group(11) != null);
      atd.setCollective(m.group(12) != null);
      atd.setNoUserModification(m.group(13) != null);
      if (m.group(14) != null) {
        atd.setUsage(AttributeUsage.parse(m.group(14).trim()));
      }

      // parse extensions
      if (m.group(15) != null) {
        atd.setExtensions(parseExtensions(m.group(15).trim()));
      }
      return atd;
      // CheckStyle:MagicNumber ON
    }
  }
}
