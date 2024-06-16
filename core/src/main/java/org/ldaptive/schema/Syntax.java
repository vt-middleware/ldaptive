/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.nio.CharBuffer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Bean for an attribute syntax schema element.
 *
 * <pre>
   SyntaxDescription = LPAREN WSP
     numericoid                 ; object identifier
     [ SP "DESC" SP qdstring ]  ; description
     extensions WSP RPAREN      ; extensions
 * </pre>
 *
 * @author  Middleware Services
 */
public class Syntax extends AbstractSchemaElement
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1129;

  /** OID. */
  private final String oid;


  /**
   * Creates a new attribute syntax.
   *
   * @param  s  oid
   */
  public Syntax(final String s)
  {
    oid = s;
  }


  /**
   * Creates a new attribute syntax.
   *
   * @param  oid  oid
   * @param  description  description
   * @param  extensions  extensions
   */
  // CheckStyle:HiddenField OFF
  public Syntax(final String oid, final String description, final Extensions extensions)
  {
    this(oid);
    setDescription(description);
    setExtensions(extensions);
  }
  // CheckStyle:HiddenField ON


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
   * Parses the supplied definition string and creates an initialized attribute syntax.
   *
   * @param  definition  to parse
   *
   * @return  attribute syntax
   *
   * @throws  SchemaParseException  if the supplied definition is invalid
   */
  public static Syntax parse(final String definition)
    throws SchemaParseException
  {
    return SchemaParser.parse(Syntax.class, definition);
  }


  @Override
  public String format()
  {
    final StringBuilder sb = new StringBuilder("( ");
    sb.append(oid).append(" ");
    if (getDescription() != null) {
      sb.append("DESC ").append(SchemaUtils.formatDescriptors(getDescription()));
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
    if (o instanceof Syntax) {
      final Syntax v = (Syntax) o;
      return LdapUtils.areEqual(oid, v.oid) &&
             LdapUtils.areEqual(getDescription(), v.getDescription()) &&
             LdapUtils.areEqual(getExtensions(), v.getExtensions());
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, oid, getDescription(), getExtensions());
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "oid=" + oid + ", " +
      "description=" + getDescription() + ", " +
      "extensions=" + getExtensions() + "]";
  }


  /**
   * Parses a syntax definition using a char buffer.
   */
  public static class DefaultDefinitionFunction extends AbstractDefaultDefinitionFunction<Syntax>
  {

    @Override
    public Syntax parse(final String definition)
      throws SchemaParseException
    {
      final CharBuffer buffer = validate(definition);
      skipSpaces(buffer);
      final Syntax s = new Syntax(readUntilSpace(buffer));
      final Extensions exts = new Extensions();
      while (buffer.hasRemaining()) {
        skipSpaces(buffer);
        final String token = readUntilSpace(buffer);
        skipSpaces(buffer);
        switch (token) {
        case "DESC":
          s.setDescription(readQDString(buffer));
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
        s.setExtensions(exts);
      }
      return s;
    }
  }


  /** Parses a syntax definition using a regular expression. */
  public static class RegexDefinitionFunction extends AbstractRegexDefinitionFunction<Syntax>
  {

    /** Pattern to match definitions. */
    private static final Pattern DEFINITION_PATTERN = Pattern.compile(
      WSP_REGEX + "\\(" +
        WSP_REGEX + "(" + NO_WSP_REGEX + ")" +
        WSP_REGEX + "(?:DESC '([^']+)')?" +
        WSP_REGEX + "(?:(X-[^ ]+.*))?" +
        WSP_REGEX + "\\)" + WSP_REGEX);


    @Override
    public Syntax parse(final String definition)
      throws SchemaParseException
    {
      final Matcher m = DEFINITION_PATTERN.matcher(definition);
      if (!m.matches()) {
        throw new SchemaParseException("Invalid attribute syntax definition: " + definition);
      }

      final Syntax asd = new Syntax(m.group(1).trim());

      // CheckStyle:MagicNumber OFF
      asd.setDescription(m.group(2) != null ? m.group(2).trim() : null);

      // parse extensions
      if (m.group(3) != null) {
        asd.setExtensions(parseExtensions(m.group(3).trim()));
      }
      return asd;
      // CheckStyle:MagicNumber ON
    }
  }
}
