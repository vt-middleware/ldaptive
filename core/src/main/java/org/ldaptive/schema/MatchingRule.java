/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Bean for a matching rule schema element.
 *
 * <pre>
   MatchingRuleDescription = LPAREN WSP
     numericoid                 ; object identifier
     [ SP "NAME" SP qdescrs ]   ; short names (descriptors)
     [ SP "DESC" SP qdstring ]  ; description
     [ SP "OBSOLETE" ]          ; not active
     SP "SYNTAX" SP numericoid  ; assertion syntax
     extensions WSP RPAREN      ; extensions
 * </pre>
 *
 * @author  Middleware Services
 */
public class MatchingRule extends AbstractNamedSchemaElement
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1117;

  /** OID. */
  private final String oid;

  /** Syntax OID. */
  private String syntaxOID;


  /**
   * Creates a new matching rule.
   *
   * @param  s  oid
   */
  public MatchingRule(final String s)
  {
    oid = s;
  }


  /**
   * Creates a new matching rule.
   *
   * @param  oid  oid
   * @param  names  names
   * @param  description  description
   * @param  obsolete  obsolete
   * @param  syntaxOID  syntax OID
   * @param  extensions  extensions
   */
  // CheckStyle:ParameterNumber|HiddenField OFF
  public MatchingRule(
    final String oid,
    final String[] names,
    final String description,
    final boolean obsolete,
    final String syntaxOID,
    final Extensions extensions)
  {
    this(oid);
    setNames(names);
    setDescription(description);
    setObsolete(obsolete);
    setSyntaxOID(syntaxOID);
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
   * Returns the syntax oid.
   *
   * @return  syntax oid
   */
  public String getSyntaxOID()
  {
    return syntaxOID;
  }


  /**
   * Sets the syntax oid.
   *
   * @param  s  syntax oid
   */
  public void setSyntaxOID(final String s)
  {
    syntaxOID = s;
  }


  /**
   * Parses the supplied definition string and creates an initialized matching rule.
   *
   * @param  definition  to parse
   *
   * @return  matching rule
   *
   * @throws  SchemaParseException  if the supplied definition is invalid
   */
  public static MatchingRule parse(final String definition)
    throws SchemaParseException
  {
    return SchemaParser.parse(MatchingRule.class, definition);
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
    if (syntaxOID != null) {
      sb.append("SYNTAX ").append(syntaxOID).append(" ");
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
    if (o instanceof MatchingRule) {
      final MatchingRule v = (MatchingRule) o;
      return LdapUtils.areEqual(oid, v.oid) &&
        LdapUtils.areEqual(getNames(), v.getNames()) &&
        LdapUtils.areEqual(getDescription(), v.getDescription()) &&
        LdapUtils.areEqual(isObsolete(), v.isObsolete()) &&
        LdapUtils.areEqual(syntaxOID, v.syntaxOID) &&
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
        syntaxOID,
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
      "syntaxOID=" + syntaxOID + ", " +
      "extensions=" + getExtensions() + "]";
  }


  /** Parses a matching rule definition using a char buffer. */
  public static class DefaultDefinitionFunction extends AbstractDefaultDefinitionFunction<MatchingRule>
  {


    @Override
    public MatchingRule parse(final String definition)
      throws SchemaParseException
    {
      final CharBuffer buffer = validate(definition);
      skipSpaces(buffer);
      final MatchingRule mr = new MatchingRule(readUntilSpace(buffer));
      final Extensions exts = new Extensions();
      while (buffer.hasRemaining()) {
        skipSpaces(buffer);
        final String token = readUntilSpace(buffer);
        skipSpaces(buffer);
        switch (token) {
        case "NAME":
          mr.setNames(readQDStrings(buffer));
          break;
        case "DESC":
          mr.setDescription(readQDString(buffer));
          break;
        case "OBSOLETE":
          mr.setObsolete(true);
          break;
        case "SYNTAX":
          mr.setSyntaxOID(readUntilSpace(buffer));
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
        mr.setExtensions(exts);
      }
      return mr;
    }
  }


  /** Parses a matching rule definition using a regular expression. */
  public static class RegexDefinitionFunction extends AbstractRegexDefinitionFunction<MatchingRule>
  {

    /** Pattern to match definitions. */
    private static final Pattern DEFINITION_PATTERN = Pattern.compile(
      WSP_REGEX + "\\(" +
        WSP_REGEX + "(" + NO_WSP_REGEX + ")" +
        WSP_REGEX + "(?:NAME" + ONE_WSP_REGEX + "(?:'([^']+)'|\\(([^\\)]+)\\)))?" +
        WSP_REGEX + "(?:DESC" + ONE_WSP_REGEX + "'([^']*)')?" +
        WSP_REGEX + "(OBSOLETE)?" +
        WSP_REGEX + "(?:SYNTAX" + ONE_WSP_REGEX + "(" + NO_WSP_REGEX + "))?" +
        WSP_REGEX + "(?:(X-[^ ]+.*))?" +
        WSP_REGEX + "\\)" + WSP_REGEX);


    @Override
    public MatchingRule parse(final String definition)
      throws SchemaParseException
    {
      final Matcher m = DEFINITION_PATTERN.matcher(definition);
      if (!m.matches()) {
        throw new SchemaParseException("Invalid matching rule definition: " + definition);
      }

      final MatchingRule mrd = new MatchingRule(m.group(1).trim());

      // CheckStyle:MagicNumber OFF
      // parse names
      if (m.group(2) != null) {
        mrd.setNames(SchemaUtils.parseDescriptors(m.group(2).trim()));
      } else if (m.group(3) != null) {
        mrd.setNames(SchemaUtils.parseDescriptors(m.group(3).trim()));
      }

      mrd.setDescription(m.group(4) != null ? m.group(4).trim() : null);
      mrd.setObsolete(m.group(5) != null);
      mrd.setSyntaxOID(m.group(6) != null ? m.group(6).trim() : null);

      // parse extensions
      if (m.group(7) != null) {
        mrd.setExtensions(parseExtensions(m.group(7).trim()));
      }
      return mrd;
      // CheckStyle:MagicNumber ON
    }
  }
}
