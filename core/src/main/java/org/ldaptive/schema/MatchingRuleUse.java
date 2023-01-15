/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Bean for a matching rule use schema element.
 *
 * <pre>
   MatchingRuleUseDescription = LPAREN WSP
     numericoid                 ; object identifier
     [ SP "NAME" SP qdescrs ]   ; short names (descriptors)
     [ SP "DESC" SP qdstring ]  ; description
     [ SP "OBSOLETE" ]          ; not active
     SP "APPLIES" SP oids       ; attribute types
     extensions WSP RPAREN      ; extensions
 * </pre>
 *
 * @author  Middleware Services
 */
public class MatchingRuleUse extends AbstractNamedSchemaElement
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1123;

  /** OID. */
  private final String oid;

  /** Superior classes. */
  private String[] appliesAttributeTypes;


  /**
   * Creates a new matching rule use.
   *
   * @param  s  oid
   */
  public MatchingRuleUse(final String s)
  {
    oid = s;
  }


  /**
   * Creates a new matching rule use.
   *
   * @param  oid  oid
   * @param  names  names
   * @param  description  description
   * @param  obsolete  obsolete
   * @param  appliesAttributeTypes  applies attribute types
   * @param  extensions  extensions
   */
  // CheckStyle:ParameterNumber|HiddenField OFF
  public MatchingRuleUse(
    final String oid,
    final String[] names,
    final String description,
    final boolean obsolete,
    final String[] appliesAttributeTypes,
    final Extensions extensions)
  {
    this(oid);
    setNames(names);
    setDescription(description);
    setObsolete(obsolete);
    setAppliesAttributeTypes(appliesAttributeTypes);
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
   * Returns the applies attribute types.
   *
   * @return  attribute types
   */
  public String[] getAppliesAttributeTypes()
  {
    return appliesAttributeTypes;
  }


  /**
   * Sets the applies attribute types.
   *
   * @param  s  attribute types
   */
  public void setAppliesAttributeTypes(final String[] s)
  {
    appliesAttributeTypes = s;
  }


  /**
   * Parses the supplied definition string and creates an initialized matching rule use.
   *
   * @param  definition  to parse
   *
   * @return  matching rule use
   *
   * @throws  SchemaParseException  if the supplied definition is invalid
   */
  public static MatchingRuleUse parse(final String definition)
    throws SchemaParseException
  {
    return SchemaParser.parse(MatchingRuleUse.class, definition);
  }


  @Override
  public String format()
  {
    final StringBuilder sb = new StringBuilder("( ");
    sb.append(oid).append(" ");
    if (getNames() != null && getNames().length > 0) {
      sb.append("NAME ");
      sb.append(SchemaUtils.formatDescriptors(getNames()));
    }
    if (getDescription() != null) {
      sb.append("DESC ");
      sb.append(SchemaUtils.formatDescriptors(getDescription()));
    }
    if (isObsolete()) {
      sb.append("OBSOLETE ");
    }
    if (appliesAttributeTypes != null && appliesAttributeTypes.length > 0) {
      sb.append("APPLIES ");
      sb.append(SchemaUtils.formatOids(appliesAttributeTypes));
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
    if (o instanceof MatchingRuleUse) {
      final MatchingRuleUse v = (MatchingRuleUse) o;
      return LdapUtils.areEqual(oid, v.oid) &&
             LdapUtils.areEqual(getNames(), v.getNames()) &&
             LdapUtils.areEqual(getDescription(), v.getDescription()) &&
             LdapUtils.areEqual(isObsolete(), v.isObsolete()) &&
             LdapUtils.areEqual(appliesAttributeTypes, v.appliesAttributeTypes) &&
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
        appliesAttributeTypes,
        getExtensions());
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("oid=").append(oid).append(", ")
      .append("names=").append(Arrays.toString(getNames())).append(", ")
      .append("description=").append(getDescription()).append(", ")
      .append("obsolete=").append(isObsolete()).append(", ")
      .append("appliesAttributeTypes=").append(Arrays.toString(appliesAttributeTypes)).append(", ")
      .append("extensions=").append(getExtensions()).append("]").toString();
  }


  /** Parses a matching rule use definition using a char buffer. */
  public static class DefaultDefinitionFunction extends AbstractDefaultDefinitionFunction<MatchingRuleUse>
  {


    @Override
    public MatchingRuleUse parse(final String definition)
      throws SchemaParseException
    {
      final CharBuffer buffer = validate(definition);
      skipSpaces(buffer);
      final MatchingRuleUse mru = new MatchingRuleUse(readUntilSpace(buffer));
      final Extensions exts = new Extensions();
      while (buffer.hasRemaining()) {
        skipSpaces(buffer);
        final String token = readUntilSpace(buffer);
        skipSpaces(buffer);
        switch (token) {
        case "NAME":
          mru.setNames(readQDStrings(buffer));
          break;
        case "DESC":
          mru.setDescription(readQDString(buffer));
          break;
        case "OBSOLETE":
          mru.setObsolete(true);
          break;
        case "APPLIES":
          mru.setAppliesAttributeTypes(readOIDs(buffer));
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
        mru.setExtensions(exts);
      }
      return mru;
    }
  }


  /** Parses a matching rule use definition using a regular expression. */
  public static class RegexDefinitionFunction extends AbstractRegexDefinitionFunction<MatchingRuleUse>
  {

    /** Pattern to match definitions. */
    private static final Pattern DEFINITION_PATTERN = Pattern.compile(
      WSP_REGEX + "\\(" +
        WSP_REGEX + "(" + NO_WSP_REGEX + ")" +
        WSP_REGEX + "(?:NAME" + ONE_WSP_REGEX + "(?:'([^']+)'|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:DESC" + ONE_WSP_REGEX + "'([^']*)')?" +
        WSP_REGEX + "(OBSOLETE)?" +
        WSP_REGEX + "(?:APPLIES" + ONE_WSP_REGEX + "(?:(" + NO_WSP_REGEX + ")|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:(X-[^ ]+.*))?" +
        WSP_REGEX + "\\)" + WSP_REGEX);


    @Override
    public MatchingRuleUse parse(final String definition)
      throws SchemaParseException
    {
      final Matcher m = DEFINITION_PATTERN.matcher(definition);
      if (!m.matches()) {
        throw new SchemaParseException("Invalid matching rule use definition: " + definition);
      }

      final MatchingRuleUse mrud = new MatchingRuleUse(m.group(1).trim());

      // CheckStyle:MagicNumber OFF
      // parse names
      if (m.group(2) != null) {
        mrud.setNames(SchemaUtils.parseDescriptors(m.group(2).trim()));
      } else if (m.group(3) != null) {
        mrud.setNames(SchemaUtils.parseDescriptors(m.group(3).trim()));
      }

      mrud.setDescription(m.group(4) != null ? m.group(4).trim() : null);
      mrud.setObsolete(m.group(5) != null);

      // parse applies attribute types
      if (m.group(6) != null) {
        mrud.setAppliesAttributeTypes(SchemaUtils.parseOIDs(m.group(6).trim()));
      } else if (m.group(7) != null) {
        mrud.setAppliesAttributeTypes(SchemaUtils.parseOIDs(m.group(7).trim()));
      }

      // parse extensions
      if (m.group(8) != null) {
        mrud.setExtensions(parseExtensions(m.group(8).trim()));
      }
      return mrud;
      // CheckStyle:MagicNumber ON
    }
  }
}
